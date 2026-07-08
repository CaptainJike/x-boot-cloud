package io.github.module.ai.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import io.github.framework.core.enums.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模块错误枚举类.
 */
@AllArgsConstructor
@Getter
public enum AiErrorEnum implements BaseEnum<Integer> {

    INVALID_ID(400, "无效ID"),
    INVALID_CODE(400, "无效AI模型配置编码"),
    NO_ENABLED_MODEL_CONFIG(400, "当前租户没有可用AI模型配置"),
    INVALID_PROVIDER_TYPE(400, "无效AI模型供应商类型"),
    DISABLED_MODEL_CONFIG(400, "AI模型配置已禁用"),
    MISSING_API_KEY(400, "AI模型配置缺少可用API Key"),
    UNSUPPORTED_CHAT_MODEL_CAPABILITY(400, "当前AI模型配置不支持对话能力"),
    DUPLICATE_MODEL_CONFIG(400, "已存在相同AI模型配置，请重新输入"),
    INVALID_CHAT_CONTENT(400, "对话内容不能为空"),
    INVALID_CHAT_ATTACHMENT(400, "无效对话附件"),
    UNSUPPORTED_IMAGE_ATTACHMENT(400, "不支持的图片附件"),
    UNSUPPORTED_IMAGE_MODALITY(400, "当前AI模型配置不支持图片理解"),
    DUPLICATE_KNOWLEDGE_BASE(400, "已存在相同知识库，请重新输入"),
    DUPLICATE_KNOWLEDGE_DOCUMENT(400, "已存在相同知识库文档，请勿重复关联"),
    DUPLICATE_AGENT(400, "已存在相同Agent，请重新输入"),
    DUPLICATE_WORKFLOW(400, "已存在相同工作流版本，请重新输入"),
    DUPLICATE_WORKFLOW_NODE(400, "已存在相同工作流节点，请重新输入"),
    INVALID_KNOWLEDGE_BASE_STATUS(400, "无效知识库状态"),
    INVALID_AGENT_STATUS(400, "无效Agent状态"),
    INVALID_WORKFLOW_STATUS(400, "无效工作流状态"),
    INVALID_WORKFLOW_NODE_STATUS(400, "无效工作流节点状态"),
    INVALID_WORKFLOW_PUBLISH_STATUS(400, "无效工作流发布状态"),
    INVALID_WORKFLOW_NODE_TYPE(400, "无效工作流节点类型"),
    WORKFLOW_ENTRY_NODE_UNAVAILABLE(400, "工作流入口节点不可用"),
    WORKFLOW_NODE_UNAVAILABLE(400, "工作流节点不可用"),
    WORKFLOW_NODE_EXECUTOR_UNAVAILABLE(400, "工作流节点执行器不可用"),
    WORKFLOW_EXECUTION_FAILED(400, "工作流执行失败"),
    KNOWLEDGE_DOCUMENT_SOURCE_UNAVAILABLE(400, "知识库文档源文件不可用"),
    KNOWLEDGE_DOCUMENT_SOURCE_EMPTY(400, "知识库文档源文件为空"),
    UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE(400, "不支持的知识库文档解析类型"),
    KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY(400, "知识库文档解析内容为空"),
    INVALID_KNOWLEDGE_DOCUMENT_CHUNK_CONFIG(400, "无效知识库文档切片配置"),
    INVALID_KNOWLEDGE_EMBEDDING_CONFIG(400, "无效知识库向量化配置"),
    UNSUPPORTED_KNOWLEDGE_EMBEDDING_PROVIDER(400, "不支持的知识库向量化供应商"),
    KNOWLEDGE_VECTOR_STORE_UNAVAILABLE(400, "知识库向量存储不可用");

    @EnumValue
    private final Integer value;
    private final String label;
}
