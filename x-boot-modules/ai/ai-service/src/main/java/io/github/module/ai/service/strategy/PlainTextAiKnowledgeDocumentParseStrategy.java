package io.github.module.ai.service.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import io.github.module.ai.service.model.AiKnowledgeParsedSection;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 纯文本和 Markdown 文档解析策略.
 */
@Component
public class PlainTextAiKnowledgeDocumentParseStrategy implements AiKnowledgeDocumentParseStrategy {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of("txt", "text", "md", "markdown");

    @Override
    public boolean supports(AiKnowledgeDocumentSource source) {
        String extension = normalizeExtension(source.getExtendName());
        return SUPPORTED_EXTENSIONS.contains(extension);
    }

    @Override
    public AiKnowledgeParsedDocument parse(AiKnowledgeDocumentSource source) throws BusinessException {
        if (ArrayUtil.isEmpty(source.getFileBytes())) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_SOURCE_EMPTY);
        }

        String content = normalizeText(new String(source.getFileBytes(), StandardCharsets.UTF_8));
        if (StrUtil.isBlank(content)) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY);
        }

        return AiKnowledgeParsedDocument.builder()
                .documentId(source.getDocumentId())
                .knowledgeBaseId(source.getKnowledgeBaseId())
                .ossFileId(source.getOssFileId())
                .documentName(source.getDocumentName())
                .originalFilename(source.getOriginalFilename())
                .extendName(source.getExtendName())
                .content(content)
                .sections(parseSections(content))
                .build();
    }

    private List<AiKnowledgeParsedSection> parseSections(String content) {
        String[] rawBlocks = content.split("\\n\\s*\\n");
        if (ArrayUtil.isEmpty(rawBlocks)) {
            return List.of(AiKnowledgeParsedSection.builder()
                    .content(content)
                    .sourcePosition("paragraph:1")
                    .build());
        }

        List<AiKnowledgeParsedSection> sections = new ArrayList<>(rawBlocks.length);
        for (String rawBlock : rawBlocks) {
            String block = rawBlock.trim();
            if (StrUtil.isBlank(block)) {
                continue;
            }
            sections.add(AiKnowledgeParsedSection.builder()
                    .title(resolveMarkdownTitle(block))
                    .content(block)
                    .sourcePosition("paragraph:" + (sections.size() + 1))
                    .build());
        }
        if (CollUtil.isEmpty(sections)) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY);
        }

        return sections;
    }

    private String resolveMarkdownTitle(String block) {
        String firstLine = StrUtil.subBefore(block, "\n", false);
        if (!firstLine.startsWith("#")) {
            return null;
        }

        return CharSequenceUtil.cleanBlank(firstLine.replaceFirst("^#+\\s*", ""));
    }

    private String normalizeText(String text) {
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+\\n", "\n")
                .trim();
    }

    private String normalizeExtension(String extension) {
        return StrUtil.blankToDefault(extension, StrUtil.EMPTY).toLowerCase(Locale.ROOT);
    }
}
