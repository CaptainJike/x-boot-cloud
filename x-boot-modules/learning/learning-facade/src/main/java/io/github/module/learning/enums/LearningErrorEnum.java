package io.github.module.learning.enums;

import io.github.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Learning OS 错误枚举.
 */
@AllArgsConstructor
@Getter
public enum LearningErrorEnum implements BaseEnum<Integer> {

    INVALID_ID(400, "无效ID"),
    INVALID_GITHUB_ACCOUNT(400, "GitHub 账号信息无效"),
    INVALID_EMAIL_ACCOUNT(400, "邮箱账号信息无效"),
    EMAIL_ACCOUNT_NOT_FOUND(404, "该邮箱尚未注册"),
    EMAIL_ACCOUNT_ALREADY_EXISTS(409, "该邮箱已注册"),
    INVALID_GOAL(400, "学习目标不存在"),
    INVALID_MAP_NODE(400, "学习节点不存在"),
    INVALID_TUTOR_SESSION(400, "Tutor 会话不存在"),
    INVALID_PRACTICE_TASK(400, "练习任务不存在或已失效"),
    INVALID_PRACTICE_ATTEMPT(400, "练习提交数据无效"),
    PRACTICE_ATTEMPT_CONFLICT(409, "练习记录已在其他设备更新，请刷新后重试"),
    INVALID_REVIEW_TASK(400, "复盘任务不存在或已失效"),
    INVALID_REVIEW_ATTEMPT(400, "复盘提交数据无效"),
    REVIEW_ATTEMPT_CONFLICT(409, "复盘记录已在其他设备更新，请刷新后重试"),
    INVALID_TODAY_CONTEXT(400, "今日学习上下文不存在"),
    INVALID_TEMPLATE(400, "未找到匹配的学习模板"),
    INVALID_TEMPLATE_ASSET(400, "学习模板资产不存在"),
    INVALID_GOAL_CONTEXT_RECORD(400, "目标上下文记录不存在"),
    INVALID_MODEL_CONFIG(400, "未找到可用的模型配置"),
    INVALID_AI_RESPONSE(500, "AI 返回结果解析失败"),
    LEARNER_DISABLED(400, "学习者账号已禁用"),
    REFLECTION_ALREADY_SUBMITTED(400, "今日反思已提交"),
    USER_NOT_LOGGED_IN(401, "用户未登录");

    private final Integer value;
    private final String label;
}
