package io.github.module.appapi.web.learning;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.appapi.util.AppStpUtil;
import io.github.module.learning.facade.LearningGoalFacade;
import io.github.module.learning.facade.LearningGoalContextFacade;
import io.github.module.learning.facade.LearningAgentFacade;
import io.github.module.learning.facade.LearningGrowthFacade;
import io.github.module.learning.facade.LearningMapFacade;
import io.github.module.learning.facade.LearningPlanFacade;
import io.github.module.learning.facade.LearningPracticeFacade;
import io.github.module.learning.facade.LearningReviewFacade;
import io.github.module.learning.facade.LearningReflectionFacade;
import io.github.module.learning.facade.LearningTemplateFacade;
import io.github.module.learning.facade.LearningTutorFacade;
import io.github.module.learning.model.request.AppCreateLearningGoalDTO;
import io.github.module.learning.model.request.AppCreateTutorSessionDTO;
import io.github.module.learning.model.request.AppGoalAdjustmentRecordDTO;
import io.github.module.learning.model.request.AppGoalBriefRecordDTO;
import io.github.module.learning.model.request.AppGoalCheckpointRecordDTO;
import io.github.module.learning.model.request.AppGoalDraftAssistDTO;
import io.github.module.learning.model.request.AppGoalExecutionHandoffDTO;
import io.github.module.learning.model.request.AppGoalTuningSnapshotDTO;
import io.github.module.learning.model.request.AppPortfolioCandidateValidationRecordDTO;
import io.github.module.learning.model.request.AppSavePracticeAttemptDTO;
import io.github.module.learning.model.request.AppSaveReviewAttemptDTO;
import io.github.module.learning.model.request.AppSubmitDailyReflectionDTO;
import io.github.module.learning.model.request.AppTemplateUpsertDTO;
import io.github.module.learning.model.request.AppSubmitTutorTurnDTO;
import io.github.module.learning.model.response.DailyReflectionBO;
import io.github.module.learning.model.response.GoalAdjustmentRecordBO;
import io.github.module.learning.model.response.GoalBriefRecordBO;
import io.github.module.learning.model.response.GoalCheckpointRecordBO;
import io.github.module.learning.model.response.GoalContextBundleBO;
import io.github.module.learning.model.response.GoalDraftAssistBO;
import io.github.module.learning.model.response.GoalExecutionHandoffBO;
import io.github.module.learning.model.response.GoalTuningSnapshotBO;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.LearnerMemoryBO;
import io.github.module.learning.model.response.LearningAgentBO;
import io.github.module.learning.model.response.LearningKnowledgeGraphBO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.LearningPlanBO;
import io.github.module.learning.model.response.LearningRhythmBO;
import io.github.module.learning.model.response.LearningTemplateBO;
import io.github.module.learning.model.response.MasteryRecordBO;
import io.github.module.learning.model.response.PracticeAttemptBO;
import io.github.module.learning.model.response.PracticeWorkspaceBO;
import io.github.module.learning.model.response.ReplanTimelineBO;
import io.github.module.learning.model.response.ReviewAttemptBO;
import io.github.module.learning.model.response.ReviewWorkspaceBO;
import io.github.module.learning.model.response.TodayLearningBO;
import io.github.module.learning.model.response.PortfolioCandidateValidationRecordBO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Learning OS App 接口.
 */
@RestController
@Tag(name = "APP Learning OS")
@RequiredArgsConstructor
@RequestMapping({ApiPrefixConstant.API_PREFIX_APP + ApiPrefixConstant.VERSION, "/app/v1"})
@SaCheckLogin(type = AppStpUtil.TYPE)
public class AppLearningController {

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningGoalFacade learningGoalFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningGoalContextFacade learningGoalContextFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningMapFacade learningMapFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningTutorFacade learningTutorFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningPracticeFacade learningPracticeFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningReviewFacade learningReviewFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningPlanFacade learningPlanFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningReflectionFacade learningReflectionFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningGrowthFacade learningGrowthFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningAgentFacade learningAgentFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningTemplateFacade learningTemplateFacade;

    @Operation(summary = "创建学习目标并生成学习地图")
    @PostMapping("/learning/goals")
    public ApiResult<LearningMapBO> createGoal(@RequestBody @Valid AppCreateLearningGoalDTO dto) {
        return ApiResult.data(learningGoalFacade.createGoal(dto));
    }

    @Operation(summary = "AI 补全 Goal Draft")
    @PostMapping("/learning/goals/assist-draft")
    public ApiResult<GoalDraftAssistBO> assistGoalDraft(@RequestBody @Valid AppGoalDraftAssistDTO dto) {
        return ApiResult.data(learningGoalFacade.assistGoalDraft(dto));
    }

    @Operation(summary = "获取目标上下文快照")
    @GetMapping("/learning/goal-context")
    public ApiResult<GoalContextBundleBO> getGoalContext() {
        return ApiResult.data(learningGoalContextFacade.getGoalContext());
    }

    @Operation(summary = "保存 Goal Brief 记录")
    @PostMapping("/learning/goal-context/briefs")
    public ApiResult<GoalBriefRecordBO> saveGoalBriefRecord(@RequestBody @Valid AppGoalBriefRecordDTO dto) {
        return ApiResult.data(learningGoalContextFacade.saveGoalBriefRecord(dto));
    }

    @Operation(summary = "保存目标调整记录")
    @PostMapping("/learning/goal-context/adjustments")
    public ApiResult<GoalAdjustmentRecordBO> saveGoalAdjustmentRecord(
            @RequestBody @Valid AppGoalAdjustmentRecordDTO dto) {
        return ApiResult.data(learningGoalContextFacade.saveGoalAdjustmentRecord(dto));
    }

    @Operation(summary = "保存当前激活的目标交接状态")
    @PutMapping("/learning/goal-context/active-handoff")
    public ApiResult<GoalExecutionHandoffBO> saveActiveGoalExecutionHandoff(
            @RequestBody @Valid AppGoalExecutionHandoffDTO dto) {
        return ApiResult.data(learningGoalContextFacade.saveActiveGoalExecutionHandoff(dto));
    }

    @Operation(summary = "清理当前激活的目标交接状态")
    @DeleteMapping("/learning/goal-context/active-handoff")
    public ApiResult<Void> clearActiveGoalExecutionHandoff() {
        learningGoalContextFacade.clearActiveGoalExecutionHandoff();
        return ApiResult.success();
    }

    @Operation(summary = "保存 Portfolio 候选目标验证记录")
    @PostMapping("/learning/goal-context/portfolio-validations")
    public ApiResult<PortfolioCandidateValidationRecordBO> savePortfolioCandidateValidationRecord(
            @RequestBody @Valid AppPortfolioCandidateValidationRecordDTO dto) {
        return ApiResult.data(learningGoalContextFacade.savePortfolioCandidateValidationRecord(dto));
    }

    @Operation(summary = "保存阶段复盘记录")
    @PostMapping("/learning/goal-context/checkpoints")
    public ApiResult<GoalCheckpointRecordBO> saveGoalCheckpointRecord(
            @RequestBody @Valid AppGoalCheckpointRecordDTO dto) {
        return ApiResult.data(learningGoalContextFacade.saveGoalCheckpointRecord(dto));
    }

    @Operation(summary = "保存当前目标调参快照")
    @PutMapping("/learning/goal-context/tuning-snapshot")
    public ApiResult<GoalTuningSnapshotBO> saveGoalTuningSnapshot(
            @RequestBody @Valid AppGoalTuningSnapshotDTO dto) {
        return ApiResult.data(learningGoalContextFacade.saveGoalTuningSnapshot(dto));
    }

    @Operation(summary = "清理当前目标调参快照")
    @DeleteMapping("/learning/goal-context/tuning-snapshot")
    public ApiResult<Void> clearGoalTuningSnapshot() {
        learningGoalContextFacade.clearGoalTuningSnapshot();
        return ApiResult.success();
    }

    @Operation(summary = "获取今日学习概览")
    @GetMapping("/learning/today")
    public ApiResult<TodayLearningBO> getToday() {
        return ApiResult.data(learningGoalFacade.getToday());
    }

    @Operation(summary = "查看学习地图")
    @GetMapping("/learning/maps/{goalId}")
    public ApiResult<LearningMapBO> getMap(@PathVariable("goalId") Long goalId) {
        return ApiResult.data(learningMapFacade.getMapByGoalId(goalId));
    }

    @Operation(summary = "创建 Tutor 会话")
    @PostMapping("/learning/tutor/sessions")
    public ApiResult<TutorSessionBO> createTutorSession(@RequestBody @Valid AppCreateTutorSessionDTO dto) {
        return ApiResult.data(learningTutorFacade.createSession(dto));
    }

    @Operation(summary = "提交 Tutor 轮次")
    @PostMapping("/learning/tutor/sessions/{sessionId}/turns")
    public ApiResult<TutorTurnBO> submitTutorTurn(@PathVariable("sessionId") Long sessionId,
                                                  @RequestBody @Valid AppSubmitTutorTurnDTO dto) {
        return ApiResult.data(learningTutorFacade.submitTurn(sessionId, dto));
    }

    @Operation(summary = "获取服务端权威练习工作区")
    @GetMapping("/learning/practice/workspace")
    public ApiResult<PracticeWorkspaceBO> getPracticeWorkspace(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningPracticeFacade.getWorkspace(goalId));
    }

    @Operation(summary = "获取服务端权威复盘工作区")
    @GetMapping("/learning/review/workspace")
    public ApiResult<ReviewWorkspaceBO> getReviewWorkspace(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningReviewFacade.getWorkspace(goalId));
    }

    @Operation(summary = "获取服务端学习计划")
    @GetMapping("/learning/plans/current")
    public ApiResult<LearningPlanBO> getCurrentLearningPlan(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningPlanFacade.getCurrentPlan(goalId));
    }

    @Operation(summary = "获取服务端计划重排时间线")
    @GetMapping("/learning/plans/replan-timeline")
    public ApiResult<ReplanTimelineBO> getReplanTimeline(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningPlanFacade.getReplanTimeline(goalId));
    }

    @Operation(summary = "保存练习草稿或完成记录")
    @PutMapping("/learning/practice/attempts/{taskKey}")
    public ApiResult<PracticeAttemptBO> savePracticeAttempt(
            @PathVariable("taskKey") String taskKey,
            @RequestBody @Valid AppSavePracticeAttemptDTO dto) {
        return ApiResult.data(learningPracticeFacade.saveAttempt(taskKey, dto));
    }

    @Operation(summary = "保存复盘草稿或完成记录")
    @PutMapping("/learning/review/attempts/{taskKey}")
    public ApiResult<ReviewAttemptBO> saveReviewAttempt(
            @PathVariable("taskKey") String taskKey,
            @RequestBody @Valid AppSaveReviewAttemptDTO dto) {
        return ApiResult.data(learningReviewFacade.saveAttempt(taskKey, dto));
    }

    @Operation(summary = "提交每日反思")
    @PostMapping("/learning/reflections/daily")
    public ApiResult<DailyReflectionBO> submitDailyReflection(@RequestBody @Valid AppSubmitDailyReflectionDTO dto) {
        return ApiResult.data(learningReflectionFacade.submitDailyReflection(dto));
    }

    @Operation(summary = "查看成长时间线")
    @GetMapping("/learning/growth/timeline")
    public ApiResult<GrowthTimelineBO> getGrowthTimeline() {
        return ApiResult.data(learningGrowthFacade.getTimeline());
    }

    @Operation(summary = "查看节点掌握记录")
    @GetMapping("/learning/mastery/records")
    public ApiResult<List<MasteryRecordBO>> getMasteryRecords(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningGrowthFacade.getMasteryRecords(goalId));
    }

    @Operation(summary = "查看学习者长期记忆快照")
    @GetMapping("/learning/memory")
    public ApiResult<LearnerMemoryBO> getLearnerMemory(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningGrowthFacade.getLearnerMemory(goalId));
    }

    @Operation(summary = "查看学习知识图谱快照")
    @GetMapping("/learning/knowledge")
    public ApiResult<LearningKnowledgeGraphBO> getLearningKnowledgeGraph(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningGrowthFacade.getLearningKnowledgeGraph(goalId));
    }

    @Operation(summary = "查看学习节奏快照")
    @GetMapping("/learning/rhythm")
    public ApiResult<LearningRhythmBO> getLearningRhythm(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningGrowthFacade.getLearningRhythm(goalId));
    }

    @Operation(summary = "查看学习 Agent 快照")
    @GetMapping("/learning/agent")
    public ApiResult<LearningAgentBO> getLearningAgent(@RequestParam("goalId") Long goalId) {
        return ApiResult.data(learningAgentFacade.getLearningAgent(goalId));
    }

    @Operation(summary = "查看我的模板资产")
    @GetMapping("/learning/templates/mine")
    public ApiResult<List<LearningTemplateBO>> getMyTemplates(@RequestParam("type") String type) {
        return ApiResult.data(learningTemplateFacade.getMyTemplates(type));
    }

    @Operation(summary = "查看模板资产详情")
    @GetMapping("/learning/templates/{templateId}")
    public ApiResult<LearningTemplateBO> getTemplate(@PathVariable("templateId") Long templateId) {
        return ApiResult.data(learningTemplateFacade.getTemplateById(templateId));
    }

    @Operation(summary = "创建模板资产")
    @PostMapping("/learning/templates")
    public ApiResult<LearningTemplateBO> createTemplate(@RequestBody @Valid AppTemplateUpsertDTO dto) {
        return ApiResult.data(learningTemplateFacade.createTemplate(dto));
    }

    @Operation(summary = "更新模板资产")
    @PutMapping("/learning/templates/{templateId}")
    public ApiResult<LearningTemplateBO> updateTemplate(@PathVariable("templateId") Long templateId,
                                                        @RequestBody @Valid AppTemplateUpsertDTO dto) {
        return ApiResult.data(learningTemplateFacade.updateTemplate(templateId, dto));
    }
}
