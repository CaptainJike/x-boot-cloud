package io.github.module.ai.service.strategy;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;

/**
 * 知识库文档解析策略.
 */
public interface AiKnowledgeDocumentParseStrategy {

    /**
     * 是否支持解析当前文档来源.
     */
    boolean supports(AiKnowledgeDocumentSource source);

    /**
     * 解析文档来源.
     */
    AiKnowledgeParsedDocument parse(AiKnowledgeDocumentSource source) throws BusinessException;
}
