package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.TutorSessionEntity;
import io.github.module.learning.entity.TutorTurnEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.TutorSessionMapper;
import io.github.module.learning.mapper.TutorTurnMapper;
import io.github.module.learning.model.request.AppCreateTutorSessionDTO;
import io.github.module.learning.model.request.AppSubmitTutorTurnDTO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;
import io.github.module.learning.service.model.TutorDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tutor 服务.
 */
@RequiredArgsConstructor
@Service
public class LearningTutorService {

    private final TutorSessionMapper tutorSessionMapper;
    private final TutorTurnMapper tutorTurnMapper;
    private final LearningGoalService learningGoalService;
    private final LearningAiService learningAiService;
    private final LearningAssembler learningAssembler;

    @Transactional(rollbackFor = Exception.class)
    public TutorSessionBO createSession(AppCreateTutorSessionDTO dto) {
        Long userId = requireUserId();
        LearningMapNodeEntity nodeEntity = learningGoalService.getOwnedNodeById(dto.getMapNodeId(), userId);
        LearningErrorEnum.INVALID_MAP_NODE.assertNotNull(nodeEntity);

        LearningGoalEntity goalEntity = learningGoalService.getOwnedGoalById(nodeEntity.getGoalId(), userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goalEntity);

        TutorSessionEntity sessionEntity = TutorSessionEntity.builder()
                .goalId(goalEntity.getId())
                .mapNodeId(nodeEntity.getId())
                .userId(userId)
                .status("ACTIVE")
                .learnerQuestion(CharSequenceUtil.nullToEmpty(dto.getLearnerQuestion()))
                .build();
        tutorSessionMapper.insert(sessionEntity);

        TutorDecision decision = learningAiService.decideTutorTurn(
                goalEntity.getTargetTopic(),
                nodeEntity.getTitle(),
                nodeEntity.getLearningObjective(),
                dto.getLearnerQuestion(),
                List.of(),
                null
        );

        TutorTurnEntity firstTurn = TutorTurnEntity.builder()
                .sessionId(sessionEntity.getId())
                .goalId(goalEntity.getId())
                .mapNodeId(nodeEntity.getId())
                .userId(userId)
                .turnNo(1)
                .learnerAnswer("")
                .diagnosis(decision.getDiagnosis())
                .actionType(decision.getActionType())
                .diagnosticQuestionsJson(JSONUtil.toJsonStr(decision.getDiagnosticQuestions()))
                .tutorResponse(decision.getTutorResponse())
                .nextStepSuggestionsJson(JSONUtil.toJsonStr(decision.getNextStepSuggestions()))
                .recommendedNodeId(resolveRecommendedNodeId(goalEntity, decision.getRecommendedNodeCode()))
                .nodeCompleted(Boolean.TRUE.equals(decision.getNodeCompleted()) ? 1 : 0)
                .build();
        tutorTurnMapper.insert(firstTurn);

        return learningAssembler.toTutorSessionBO(sessionEntity, nodeEntity, List.of(firstTurn));
    }

    @Transactional(rollbackFor = Exception.class)
    public TutorTurnBO submitTurn(Long sessionId, AppSubmitTutorTurnDTO dto) {
        Long userId = requireUserId();
        TutorSessionEntity sessionEntity = tutorSessionMapper.selectOne(
                new QueryWrapper<TutorSessionEntity>()
                        .lambda()
                        .eq(TutorSessionEntity::getId, sessionId)
                        .eq(TutorSessionEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
        LearningErrorEnum.INVALID_TUTOR_SESSION.assertNotNull(sessionEntity);

        LearningGoalEntity goalEntity = learningGoalService.getOwnedGoalById(sessionEntity.getGoalId(), userId);
        LearningMapNodeEntity nodeEntity = learningGoalService.getOwnedNodeById(sessionEntity.getMapNodeId(), userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goalEntity);
        LearningErrorEnum.INVALID_MAP_NODE.assertNotNull(nodeEntity);

        List<TutorTurnEntity> existingTurns = tutorTurnMapper.selectList(
                new QueryWrapper<TutorTurnEntity>()
                        .lambda()
                        .eq(TutorTurnEntity::getSessionId, sessionId)
                        .orderByAsc(TutorTurnEntity::getTurnNo)
        );

        List<String> previousQuestions = existingTurns.stream()
                .flatMap(turn -> JSONUtil.toList(JSONUtil.parseArray(CharSequenceUtil.blankToDefault(turn.getDiagnosticQuestionsJson(), "[]")), String.class).stream())
                .toList();

        TutorDecision decision = learningAiService.decideTutorTurn(
                goalEntity.getTargetTopic(),
                nodeEntity.getTitle(),
                nodeEntity.getLearningObjective(),
                sessionEntity.getLearnerQuestion(),
                previousQuestions,
                dto.getLearnerAnswer()
        );

        TutorTurnEntity turnEntity = TutorTurnEntity.builder()
                .sessionId(sessionId)
                .goalId(sessionEntity.getGoalId())
                .mapNodeId(sessionEntity.getMapNodeId())
                .userId(userId)
                .turnNo(existingTurns.size() + 1)
                .learnerAnswer(dto.getLearnerAnswer())
                .diagnosis(decision.getDiagnosis())
                .actionType(decision.getActionType())
                .diagnosticQuestionsJson(JSONUtil.toJsonStr(decision.getDiagnosticQuestions()))
                .tutorResponse(decision.getTutorResponse())
                .nextStepSuggestionsJson(JSONUtil.toJsonStr(decision.getNextStepSuggestions()))
                .recommendedNodeId(resolveRecommendedNodeId(goalEntity, decision.getRecommendedNodeCode()))
                .nodeCompleted(Boolean.TRUE.equals(decision.getNodeCompleted()) ? 1 : 0)
                .build();
        tutorTurnMapper.insert(turnEntity);

        updateLearningProgress(goalEntity, nodeEntity, turnEntity);
        return learningAssembler.toTutorTurnBO(turnEntity);
    }

    private void updateLearningProgress(LearningGoalEntity goalEntity,
                                        LearningMapNodeEntity nodeEntity,
                                        TutorTurnEntity turnEntity) {
        Long userId = requireUserId();
        LearningNodeProgressEntity progressEntity = learningGoalService.getProgress(goalEntity.getId(), nodeEntity.getId(), userId);
        if (progressEntity == null) {
            return;
        }
        progressEntity
                .setLastDiagnosis(turnEntity.getDiagnosis())
                .setLastStudiedAt(LocalDateTimeUtil.now());
        if (turnEntity.getNodeCompleted() != null && turnEntity.getNodeCompleted() == 1) {
            progressEntity
                    .setStatus("COMPLETED")
                    .setCompletedAt(LocalDateTime.now())
                    .setMasteryLevel(Math.max(progressEntity.getMasteryLevel(), 80));
            LearningMapNodeEntity nextNode = findNextNode(goalEntity, nodeEntity.getSortOrder());
            if (nextNode != null) {
                goalEntity.setActiveNodeId(nextNode.getId());
                learningGoalService.updateGoal(goalEntity);
                LearningNodeProgressEntity nextProgress = learningGoalService.getProgress(goalEntity.getId(), nextNode.getId(), userId);
                if (nextProgress != null && CharSequenceUtil.equals(nextProgress.getStatus(), "PENDING")) {
                    nextProgress.setStatus("READY");
                    learningGoalService.updateProgress(nextProgress);
                }
            } else {
                goalEntity.setStatus("COMPLETED");
                learningGoalService.updateGoal(goalEntity);
            }
        } else {
            progressEntity.setStatus(CharSequenceUtil.equals(turnEntity.getDiagnosis(), "misconception") ? "REVIEWING" : "IN_PROGRESS");
            progressEntity.setMasteryLevel(Math.min(progressEntity.getMasteryLevel() + 10, 70));
        }
        learningGoalService.updateProgress(progressEntity);
    }

    private LearningMapNodeEntity findNextNode(LearningGoalEntity goalEntity, Integer currentSortOrder) {
        return learningGoalService.listNodesByGoalId(goalEntity.getId(), requireUserId()).stream()
                .filter(node -> node.getSortOrder() > currentSortOrder)
                .min((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .orElse(null);
    }

    private Long resolveRecommendedNodeId(LearningGoalEntity goalEntity, String nodeCode) {
        if (CharSequenceUtil.isBlank(nodeCode)) {
            return null;
        }
        return learningGoalService.listNodesByGoalId(goalEntity.getId(), requireUserId()).stream()
                .filter(node -> CharSequenceUtil.equals(node.getNodeCode(), nodeCode))
                .map(LearningMapNodeEntity::getId)
                .findFirst()
                .orElse(null);
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }
}
