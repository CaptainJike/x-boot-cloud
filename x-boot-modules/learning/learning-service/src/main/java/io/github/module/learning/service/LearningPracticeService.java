package io.github.module.learning.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.learning.entity.LearningGoalEntity;
import io.github.module.learning.entity.LearningMapNodeEntity;
import io.github.module.learning.entity.LearningNodeProgressEntity;
import io.github.module.learning.entity.PracticeAttemptEntity;
import io.github.module.learning.entity.PracticeTaskEntity;
import io.github.module.learning.enums.LearningErrorEnum;
import io.github.module.learning.mapper.PracticeAttemptMapper;
import io.github.module.learning.mapper.PracticeTaskMapper;
import io.github.module.learning.model.request.AppSavePracticeAttemptDTO;
import io.github.module.learning.model.response.PracticeAttemptBO;
import io.github.module.learning.model.response.PracticeTaskBO;
import io.github.module.learning.model.response.PracticeWorkspaceBO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 服务端权威练习任务与提交记录服务.
 */
@RequiredArgsConstructor
@Service
public class LearningPracticeService {

    private static final long DEFAULT_TENANT_ID = 0L;
    private static final int WORKSPACE_SCHEMA_VERSION = 1;

    private final PracticeTaskMapper practiceTaskMapper;
    private final PracticeAttemptMapper practiceAttemptMapper;
    private final LearningGoalService learningGoalService;
    private final LearningEventService learningEventService;

    /**
     * 读取时会物化当前节点的稳定任务，历史尝试仍按目标完整返回.
     */
    @Transactional(rollbackFor = Exception.class)
    public PracticeWorkspaceBO getWorkspace(Long goalId) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(goalId, userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        LearningMapNodeEntity node = learningGoalService.getCurrentNode(goal);
        LearningErrorEnum.INVALID_TODAY_CONTEXT.assertNotNull(node);
        LearningErrorEnum.INVALID_MAP_NODE.assertTrue(Objects.equals(node.getUserId(), userId));

        LearningNodeProgressEntity progress = learningGoalService.getProgress(goalId, node.getId(), userId);
        List<PracticeTaskEntity> currentTasks = synchronizeCurrentTasks(userId, goal, node, progress);
        Map<Long, PracticeTaskEntity> taskById = currentTasks.stream()
                .filter(task -> task.getId() != null)
                .collect(Collectors.toMap(PracticeTaskEntity::getId, Function.identity(), (left, right) -> right));

        List<PracticeAttemptEntity> attempts = practiceAttemptMapper.selectList(
                new QueryWrapper<PracticeAttemptEntity>()
                        .lambda()
                        .eq(PracticeAttemptEntity::getGoalId, goalId)
                        .eq(PracticeAttemptEntity::getUserId, userId)
                        .orderByAsc(PracticeAttemptEntity::getUpdatedAt)
        );

        String verification = CharSequenceUtil.blankToDefault(
                node.getVerificationMethod(), "用自己的话解释并给出一个可检查的例子");
        return PracticeWorkspaceBO.builder()
                .goalId(goalId)
                .schemaVersion(WORKSPACE_SCHEMA_VERSION)
                .mode("server")
                .generatedAt(LocalDateTime.now())
                .focusNodeId(node.getId())
                .focusNodeTitle(node.getTitle())
                .summary("服务端已围绕当前节点「" + node.getTitle() + "」生成可跨设备同步的验证练习。")
                .masteryFocus(buildMasteryFocus(node, progress, verification))
                .recommendedNextStep("完成练习后提交 Reflection，把评测暴露出的真实卡点写回学习闭环。")
                .tasks(currentTasks.stream().map(this::toTaskBO).toList())
                .attempts(CollUtil.emptyIfNull(attempts).stream()
                        .map(attempt -> toAttemptBO(attempt, taskById.get(attempt.getPracticeTaskId())))
                        .toList())
                .build();
    }

    /**
     * mutationId 保证安全重试，baseVersion 防止跨设备静默覆盖.
     */
    @Transactional(rollbackFor = Exception.class)
    public PracticeAttemptBO saveAttempt(String taskKey, AppSavePracticeAttemptDTO dto) {
        Long userId = requireUserId();
        LearningGoalEntity goal = learningGoalService.getOwnedGoalById(dto.getGoalId(), userId);
        LearningErrorEnum.INVALID_GOAL.assertNotNull(goal);

        PracticeTaskEntity task = practiceTaskMapper.selectOne(
                new QueryWrapper<PracticeTaskEntity>()
                        .lambda()
                        .eq(PracticeTaskEntity::getTaskKey, taskKey)
                        .eq(PracticeTaskEntity::getGoalId, dto.getGoalId())
                        .eq(PracticeTaskEntity::getUserId, userId)
                        .eq(PracticeTaskEntity::getActive, 1)
                        .last(" LIMIT 1")
        );
        LearningErrorEnum.INVALID_PRACTICE_TASK.assertNotNull(task);

        PracticeAttemptEntity existing = findAttempt(task.getId(), userId);
        if (existing != null && Objects.equals(existing.getLastMutationId(), dto.getMutationId())) {
            return toAttemptBO(existing, task);
        }

        validateCompletedAttempt(dto);
        if (existing == null) {
            LearningErrorEnum.PRACTICE_ATTEMPT_CONFLICT.assertTrue(dto.getBaseVersion() == 0L);
            PracticeAttemptEntity inserted = buildAttemptEntity(userId, task, dto);
            inserted.setSyncVersion(1L);
            try {
                practiceAttemptMapper.insert(inserted);
                learningEventService.recordPracticeAttempt(task, inserted);
                return toAttemptBO(inserted, task);
            } catch (DuplicateKeyException duplicateKeyException) {
                PracticeAttemptEntity concurrent = findAttempt(task.getId(), userId);
                if (concurrent != null && Objects.equals(concurrent.getLastMutationId(), dto.getMutationId())) {
                    return toAttemptBO(concurrent, task);
                }
                throw duplicateKeyException;
            }
        }

        LearningErrorEnum.PRACTICE_ATTEMPT_CONFLICT.assertTrue(
                Objects.equals(existing.getSyncVersion(), dto.getBaseVersion()));
        Long expectedVersion = dto.getBaseVersion();
        applyAttempt(existing, dto);
        existing.setSyncVersion(expectedVersion + 1);

        LambdaUpdateWrapper<PracticeAttemptEntity> versionGuard = Wrappers.lambdaUpdate(PracticeAttemptEntity.class)
                .eq(PracticeAttemptEntity::getId, existing.getId())
                .eq(PracticeAttemptEntity::getUserId, userId)
                .eq(PracticeAttemptEntity::getSyncVersion, expectedVersion);
        int updated = practiceAttemptMapper.update(existing, versionGuard);
        if (updated != 1) {
            PracticeAttemptEntity concurrent = findAttempt(task.getId(), userId);
            if (concurrent != null && Objects.equals(concurrent.getLastMutationId(), dto.getMutationId())) {
                return toAttemptBO(concurrent, task);
            }
            LearningErrorEnum.PRACTICE_ATTEMPT_CONFLICT.assertTrue(false);
        }
        learningEventService.recordPracticeAttempt(task, existing);
        return toAttemptBO(existing, task);
    }

    private PracticeAttemptEntity findAttempt(Long taskId, Long userId) {
        return practiceAttemptMapper.selectOne(
                new QueryWrapper<PracticeAttemptEntity>()
                        .lambda()
                        .eq(PracticeAttemptEntity::getPracticeTaskId, taskId)
                        .eq(PracticeAttemptEntity::getUserId, userId)
                        .last(" LIMIT 1")
        );
    }

    private List<PracticeTaskEntity> synchronizeCurrentTasks(Long userId,
                                                              LearningGoalEntity goal,
                                                              LearningMapNodeEntity node,
                                                              LearningNodeProgressEntity progress) {
        List<PracticeTaskEntity> existingTasks = practiceTaskMapper.selectList(
                new QueryWrapper<PracticeTaskEntity>()
                        .lambda()
                        .eq(PracticeTaskEntity::getGoalId, goal.getId())
                        .eq(PracticeTaskEntity::getUserId, userId)
        );
        Map<String, PracticeTaskEntity> taskByKey = CollUtil.emptyIfNull(existingTasks).stream()
                .collect(Collectors.toMap(PracticeTaskEntity::getTaskKey, Function.identity(), (left, right) -> right));
        List<TaskSpec> specs = buildTaskSpecs(node, progress);
        Set<String> currentKeys = specs.stream().map(TaskSpec::taskKey).collect(Collectors.toSet());

        for (PracticeTaskEntity oldTask : CollUtil.emptyIfNull(existingTasks)) {
            if (oldTask.getActive() != null && oldTask.getActive() == 1 && !currentKeys.contains(oldTask.getTaskKey())) {
                oldTask.setActive(0);
                practiceTaskMapper.updateById(oldTask);
            }
        }

        List<PracticeTaskEntity> currentTasks = new ArrayList<>();
        for (TaskSpec spec : specs) {
            PracticeTaskEntity task = taskByKey.get(spec.taskKey());
            if (task == null) {
                task = PracticeTaskEntity.builder()
                        .tenantId(DEFAULT_TENANT_ID)
                        .userId(userId)
                        .goalId(goal.getId())
                        .taskKey(spec.taskKey())
                        .taskVersion(1)
                        .build();
                applyTaskSpec(task, node, spec);
                practiceTaskMapper.insert(task);
            } else {
                applyTaskSpec(task, node, spec);
                task.setTaskVersion(Math.max(1, task.getTaskVersion() == null ? 1 : task.getTaskVersion()));
                practiceTaskMapper.updateById(task);
            }
            currentTasks.add(task);
        }
        return currentTasks;
    }

    private List<TaskSpec> buildTaskSpecs(LearningMapNodeEntity node, LearningNodeProgressEntity progress) {
        String title = node.getTitle();
        String verification = CharSequenceUtil.blankToDefault(
                node.getVerificationMethod(), "给出一个可以检查结果的最小验证");
        String completion = CharSequenceUtil.blankToDefault(
                node.getCompletionCriteria(), "能够解释核心概念并完成最小应用");
        String diagnosis = progress == null ? null : progress.getLastDiagnosis();
        int baseMinutes = node.getEstimatedMinutes() == null ? 10 : node.getEstimatedMinutes() / 3;
        int minutes = Math.max(8, Math.min(12, baseMinutes));

        return List.of(
                new TaskSpec(
                        "practice-check-" + node.getId(),
                        "check",
                        "先检查你是否真的理解「" + title + "」",
                        "不用查资料，直接回答：它解决什么问题、核心作用是什么、不使用会有什么后果？",
                        "能用自己的话讲清楚作用、问题背景和价值。",
                        CharSequenceUtil.blankToDefault(node.getWhyItMatters(), "先回答为什么，再补定义。"),
                        minutes,
                        diagnosis,
                        List.of(title, verification),
                        "concept_check"
                ),
                new TaskSpec(
                        "practice-explain-" + node.getId(),
                        "explain",
                        "把「" + title + "」讲给昨天的自己听",
                        "请说明它是什么、不是什么、什么时候该用、什么时候不该用，并指出一个容易误解的边界。",
                        "能清晰交代定义、边界和使用条件。",
                        "先解释边界，再举例，通常比先背概念更稳。",
                        10,
                        diagnosis,
                        List.of(title, completion),
                        "explanation"
                ),
                new TaskSpec(
                        "practice-apply-" + node.getId(),
                        "apply",
                        "把「" + title + "」放进一个真实场景",
                        "设计一个最小场景，写出为什么要用它、你会怎么做，以及如何验证自己用对了。",
                        "能把概念认识推进到场景应用，并留下可复核证据。",
                        "可以直接围绕「" + verification + "」设计最小案例。",
                        12,
                        diagnosis,
                        List.of(title, verification, completion),
                        Objects.equals(diagnosis, "needs_prereq") ? "prerequisite_repair" : "application"
                )
        );
    }

    private void applyTaskSpec(PracticeTaskEntity task, LearningMapNodeEntity node, TaskSpec spec) {
        task.setMapNodeId(node.getId());
        task.setTaskType(spec.taskType());
        task.setTitle(spec.title());
        task.setPrompt(spec.prompt());
        task.setExpectedOutcome(spec.expectedOutcome());
        task.setHint(spec.hint());
        task.setEstimatedMinutes(spec.estimatedMinutes());
        task.setNodeTitle(node.getTitle());
        task.setSourceDiagnosis(spec.sourceDiagnosis());
        task.setRelatedConceptsJson(JSONUtil.toJsonStr(spec.relatedConcepts()));
        task.setEvidenceKind(spec.evidenceKind());
        task.setKnowledgeFocus(null);
        task.setHandoffValidation(0);
        task.setHandoffTitle(null);
        task.setActive(1);
    }

    private PracticeAttemptEntity buildAttemptEntity(Long userId,
                                                      PracticeTaskEntity task,
                                                      AppSavePracticeAttemptDTO dto) {
        PracticeAttemptEntity entity = PracticeAttemptEntity.builder()
                .tenantId(DEFAULT_TENANT_ID)
                .userId(userId)
                .goalId(task.getGoalId())
                .practiceTaskId(task.getId())
                .mapNodeId(task.getMapNodeId())
                .attemptKey(task.getTaskKey())
                .build();
        applyAttempt(entity, dto);
        return entity;
    }

    private void applyAttempt(PracticeAttemptEntity entity, AppSavePracticeAttemptDTO dto) {
        entity.setResponseContent(CharSequenceUtil.nullToEmpty(dto.getResponse()).trim());
        entity.setSelfRating(dto.getSelfRating());
        entity.setArtifactsJson(JSONUtil.toJsonStr(CollUtil.emptyIfNull(dto.getArtifacts())));
        entity.setAssessmentJson(dto.getAssessment() == null ? null : JSONUtil.toJsonStr(dto.getAssessment()));
        entity.setCompleted(Boolean.TRUE.equals(dto.getCompleted()) ? 1 : 0);
        entity.setHandoffValidation(Boolean.TRUE.equals(dto.getHandoffValidation()) ? 1 : 0);
        entity.setClientUpdatedAt(CharSequenceUtil.blankToDefault(
                dto.getClientUpdatedAt(), LocalDateTime.now().toString()));
        entity.setLastMutationId(dto.getMutationId());
    }

    private void validateCompletedAttempt(AppSavePracticeAttemptDTO dto) {
        if (!Boolean.TRUE.equals(dto.getCompleted())) {
            return;
        }
        LearningErrorEnum.INVALID_PRACTICE_ATTEMPT.assertNotBlank(dto.getResponse());
        LearningErrorEnum.INVALID_PRACTICE_ATTEMPT.assertNotEmpty(dto.getArtifacts());
        LearningErrorEnum.INVALID_PRACTICE_ATTEMPT.assertNotNull(dto.getAssessment());
    }

    private PracticeTaskBO toTaskBO(PracticeTaskEntity task) {
        return PracticeTaskBO.builder()
                .id(task.getTaskKey())
                .type(task.getTaskType())
                .title(task.getTitle())
                .prompt(task.getPrompt())
                .expectedOutcome(task.getExpectedOutcome())
                .hint(task.getHint())
                .estimatedMinutes(task.getEstimatedMinutes())
                .nodeId(task.getMapNodeId())
                .nodeTitle(task.getNodeTitle())
                .sourceDiagnosis(task.getSourceDiagnosis())
                .relatedConcepts(parseStringList(task.getRelatedConceptsJson()))
                .evidenceKind(task.getEvidenceKind())
                .knowledgeFocus(task.getKnowledgeFocus())
                .handoffValidation(task.getHandoffValidation() != null && task.getHandoffValidation() == 1)
                .handoffTitle(task.getHandoffTitle())
                .build();
    }

    private PracticeAttemptBO toAttemptBO(PracticeAttemptEntity attempt, PracticeTaskEntity task) {
        return PracticeAttemptBO.builder()
                .taskId(task == null ? attempt.getAttemptKey() : task.getTaskKey())
                .nodeId(attempt.getMapNodeId())
                .taskType(task == null ? null : task.getTaskType())
                .evidenceKind(task == null ? null : task.getEvidenceKind())
                .response(attempt.getResponseContent())
                .selfRating(attempt.getSelfRating())
                .artifacts(parseArtifacts(attempt.getArtifactsJson()))
                .assessment(parseAssessment(attempt.getAssessmentJson()))
                .completed(attempt.getCompleted() != null && attempt.getCompleted() == 1)
                .handoffValidation(attempt.getHandoffValidation() != null && attempt.getHandoffValidation() == 1)
                .updatedAt(attempt.getUpdatedAt())
                .serverId(attempt.getId())
                .serverVersion(attempt.getSyncVersion())
                .lastMutationId(attempt.getLastMutationId())
                .syncStatus("synced")
                .build();
    }

    private List<String> buildMasteryFocus(LearningMapNodeEntity node,
                                           LearningNodeProgressEntity progress,
                                           String verification) {
        LinkedHashSet<String> focus = new LinkedHashSet<>();
        if (progress != null && CharSequenceUtil.isNotBlank(progress.getLastDiagnosis())) {
            focus.add("优先修复最近诊断暴露的「" + progress.getLastDiagnosis() + "」问题");
        }
        if (CharSequenceUtil.isNotBlank(node.getLearningObjective())) {
            focus.add(node.getLearningObjective());
        }
        focus.add(verification);
        if (CharSequenceUtil.isNotBlank(node.getCompletionCriteria())) {
            focus.add(node.getCompletionCriteria());
        }
        return focus.stream().limit(4).toList();
    }

    private List<String> parseStringList(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), String.class);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private List<PracticeAttemptBO.ArtifactBO> parseArtifacts(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return Collections.emptyList();
        }
        try {
            return JSONUtil.toList(JSONUtil.parseArray(json), PracticeAttemptBO.ArtifactBO.class);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    private PracticeAttemptBO.AssessmentBO parseAssessment(String json) {
        if (CharSequenceUtil.isBlank(json)) {
            return null;
        }
        try {
            return JSONUtil.toBean(json, PracticeAttemptBO.AssessmentBO.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private Long requireUserId() {
        Long userId = UserContextHolder.getUserId();
        LearningErrorEnum.USER_NOT_LOGGED_IN.assertNotNull(userId);
        return userId;
    }

    private record TaskSpec(String taskKey,
                            String taskType,
                            String title,
                            String prompt,
                            String expectedOutcome,
                            String hint,
                            Integer estimatedMinutes,
                            String sourceDiagnosis,
                            List<String> relatedConcepts,
                            String evidenceKind) {
    }
}
