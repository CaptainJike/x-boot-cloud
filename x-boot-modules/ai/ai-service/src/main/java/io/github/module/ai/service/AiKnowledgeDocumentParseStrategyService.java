package io.github.module.ai.service;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkConfig;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import io.github.module.ai.service.strategy.AiKnowledgeDocumentChunkStrategy;
import io.github.module.ai.service.strategy.AiKnowledgeDocumentParseStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 知识库文档解析与切片策略服务.
 */
@RequiredArgsConstructor
@Service
public class AiKnowledgeDocumentParseStrategyService {

    private final List<AiKnowledgeDocumentParseStrategy> parseStrategies;

    private final AiKnowledgeDocumentChunkStrategy chunkStrategy;

    /**
     * 解析文档来源.
     */
    public AiKnowledgeParsedDocument parse(AiKnowledgeDocumentSource source) throws BusinessException {
        return parseStrategies.stream()
                .filter(strategy -> strategy.supports(source))
                .findFirst()
                .orElseThrow(() -> new BusinessException(AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE))
                .parse(source);
    }

    /**
     * 解析并生成切片草稿.
     */
    public List<AiKnowledgeDocumentChunkDraft> parseAndChunk(AiKnowledgeDocumentSource source,
                                                             AiKnowledgeDocumentChunkConfig config)
            throws BusinessException {
        return chunkStrategy.chunk(this.parse(source), config);
    }
}
