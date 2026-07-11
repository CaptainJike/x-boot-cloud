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
    INVALID_GOAL(400, "学习目标不存在"),
    INVALID_MAP_NODE(400, "学习节点不存在"),
    INVALID_TUTOR_SESSION(400, "Tutor 会话不存在"),
    INVALID_TODAY_CONTEXT(400, "今日学习上下文不存在"),
    INVALID_TEMPLATE(400, "未找到匹配的学习模板"),
    INVALID_MODEL_CONFIG(400, "未找到可用的模型配置"),
    INVALID_AI_RESPONSE(500, "AI 返回结果解析失败"),
    LEARNER_DISABLED(400, "学习者账号已禁用"),
    REFLECTION_ALREADY_SUBMITTED(400, "今日反思已提交"),
    USER_NOT_LOGGED_IN(401, "用户未登录");

    private final Integer value;
    private final String label;
}
