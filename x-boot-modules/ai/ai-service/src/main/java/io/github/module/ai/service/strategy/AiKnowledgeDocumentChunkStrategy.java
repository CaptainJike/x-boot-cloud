package io.github.module.ai.service.strategy;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkConfig;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;

import java.util.List;

/**
 * 知识库文档切片策略.
 */
public interface AiKnowledgeDocumentChunkStrategy {

    /**
     * 切分文档.
     */
    List<AiKnowledgeDocumentChunkDraft> chunk(AiKnowledgeParsedDocument document,
                                              AiKnowledgeDocumentChunkConfig config) throws BusinessException;
}
