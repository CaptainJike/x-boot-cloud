package io.github.module.ai.service.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkConfig;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import io.github.module.ai.service.model.AiKnowledgeParsedSection;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认知识库文档切片策略.
 */
@Component
public class DefaultAiKnowledgeDocumentChunkStrategy implements AiKnowledgeDocumentChunkStrategy {

    private static final int PREVIEW_LENGTH = 200;

    @Override
    public List<AiKnowledgeDocumentChunkDraft> chunk(AiKnowledgeParsedDocument document,
                                                     AiKnowledgeDocumentChunkConfig config) throws BusinessException {
        EffectiveChunkConfig effectiveConfig = resolveConfig(config);
        List<AiKnowledgeParsedSection> sections = document.getSections();
        if (CollUtil.isEmpty(sections)) {
            sections = List.of(AiKnowledgeParsedSection.builder()
                    .content(document.getContent())
                    .sourcePosition("paragraph:1")
                    .build());
        }

        List<AiKnowledgeDocumentChunkDraft> drafts = new ArrayList<>();
        String overlapText = StrUtil.EMPTY;
        for (AiKnowledgeParsedSection section : sections) {
            String content = normalizeContent(section.getContent());
            if (StrUtil.isBlank(content)) {
                continue;
            }
            List<String> pieces = splitSection(content, effectiveConfig.maxChars());
            for (String piece : pieces) {
                String chunkContent = normalizeContent(overlapText + piece);
                if (StrUtil.isBlank(chunkContent)) {
                    continue;
                }
                drafts.add(buildDraft(document, section, drafts.size() + 1, chunkContent, effectiveConfig));
                overlapText = tailByChars(chunkContent, effectiveConfig.overlapChars());
            }
        }

        if (drafts.isEmpty()) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY);
        }

        return drafts;
    }

    private AiKnowledgeDocumentChunkDraft buildDraft(AiKnowledgeParsedDocument document,
                                                    AiKnowledgeParsedSection section,
                                                    int chunkNo,
                                                    String content,
                                                    EffectiveChunkConfig config) {
        return AiKnowledgeDocumentChunkDraft.builder()
                .knowledgeBaseId(document.getKnowledgeBaseId())
                .documentId(document.getDocumentId())
                .chunkNo(chunkNo)
                .content(content)
                .contentPreview(StrUtil.maxLength(content, PREVIEW_LENGTH))
                .sourcePage(section.getSourcePage())
                .sourcePosition(section.getSourcePosition())
                .tokenCount(estimateTokens(content, config.tokenCharRatio()))
                .build();
    }

    private List<String> splitSection(String content, int maxChars) {
        if (content.length() <= maxChars) {
            return List.of(content);
        }

        List<String> pieces = new ArrayList<>();
        int start = 0;
        while (start < content.length()) {
            int end = Math.min(start + maxChars, content.length());
            int splitAt = chooseSplitPosition(content, start, end);
            pieces.add(content.substring(start, splitAt).trim());
            start = splitAt;
        }

        return pieces;
    }

    private int chooseSplitPosition(String content, int start, int end) {
        if (end >= content.length()) {
            return end;
        }

        int paragraphBreak = content.lastIndexOf("\n\n", end);
        if (paragraphBreak > start) {
            return paragraphBreak + 2;
        }
        int lineBreak = content.lastIndexOf('\n', end);
        if (lineBreak > start) {
            return lineBreak + 1;
        }
        int sentenceBreak = Math.max(content.lastIndexOf('。', end), content.lastIndexOf('.', end));
        if (sentenceBreak > start) {
            return sentenceBreak + 1;
        }

        return end;
    }

    private EffectiveChunkConfig resolveConfig(AiKnowledgeDocumentChunkConfig config) {
        int maxTokens = defaultIfNull(config == null ? null : config.getMaxTokens(),
                AiKnowledgeDocumentChunkConfig.DEFAULT_MAX_TOKENS);
        int overlapTokens = defaultIfNull(config == null ? null : config.getOverlapTokens(),
                AiKnowledgeDocumentChunkConfig.DEFAULT_OVERLAP_TOKENS);
        int tokenCharRatio = defaultIfNull(config == null ? null : config.getTokenCharRatio(),
                AiKnowledgeDocumentChunkConfig.DEFAULT_TOKEN_CHAR_RATIO);
        if (maxTokens <= 0 || overlapTokens < 0 || tokenCharRatio <= 0 || overlapTokens >= maxTokens) {
            throw new BusinessException(AiErrorEnum.INVALID_KNOWLEDGE_DOCUMENT_CHUNK_CONFIG);
        }

        return new EffectiveChunkConfig(maxTokens * tokenCharRatio, overlapTokens * tokenCharRatio, tokenCharRatio);
    }

    private String normalizeContent(String content) {
        return StrUtil.blankToDefault(content, StrUtil.EMPTY).trim();
    }

    private String tailByChars(String content, int overlapChars) {
        if (overlapChars <= 0 || StrUtil.isBlank(content)) {
            return StrUtil.EMPTY;
        }

        return StrUtil.subSuf(content, Math.max(0, content.length() - overlapChars));
    }

    private int estimateTokens(String content, int tokenCharRatio) {
        String text = CharSequenceUtil.cleanBlank(content);
        if (StrUtil.isBlank(text)) {
            return 0;
        }

        return (int) Math.ceil((double) text.length() / tokenCharRatio);
    }

    private int defaultIfNull(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private record EffectiveChunkConfig(int maxChars, int overlapChars, int tokenCharRatio) {
    }
}
