package io.github.module.appapi.web.learning;

import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.appapi.util.AppStpUtil;
import io.github.module.learning.facade.LearningGoalFacade;
import io.github.module.learning.facade.LearningGrowthFacade;
import io.github.module.learning.facade.LearningMapFacade;
import io.github.module.learning.facade.LearningReflectionFacade;
import io.github.module.learning.facade.LearningTutorFacade;
import io.github.module.learning.model.request.AppCreateLearningGoalDTO;
import io.github.module.learning.model.request.AppCreateTutorSessionDTO;
import io.github.module.learning.model.request.AppSubmitDailyReflectionDTO;
import io.github.module.learning.model.request.AppSubmitTutorTurnDTO;
import io.github.module.learning.model.response.DailyReflectionBO;
import io.github.module.learning.model.response.GrowthTimelineBO;
import io.github.module.learning.model.response.LearningMapBO;
import io.github.module.learning.model.response.TodayLearningBO;
import io.github.module.learning.model.response.TutorSessionBO;
import io.github.module.learning.model.response.TutorTurnBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private LearningMapFacade learningMapFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningTutorFacade learningTutorFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningReflectionFacade learningReflectionFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private LearningGrowthFacade learningGrowthFacade;

    @Operation(summary = "创建学习目标并生成学习地图")
    @PostMapping("/learning/goals")
    public ApiResult<LearningMapBO> createGoal(@RequestBody @Valid AppCreateLearningGoalDTO dto) {
        return ApiResult.data(learningGoalFacade.createGoal(dto));
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
}
