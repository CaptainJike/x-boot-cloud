package io.github.module.ai.service.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import io.github.module.ai.service.model.AiKnowledgeParsedSection;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * PDF / Word 文档解析策略.
 */
@Component
public class OfficeAiKnowledgeDocumentParseStrategy implements AiKnowledgeDocumentParseStrategy {

    private static final List<String> SUPPORTED_EXTENSIONS = List.of("pdf", "doc", "docx");

    @Override
    public boolean supports(AiKnowledgeDocumentSource source) {
        return SUPPORTED_EXTENSIONS.contains(normalizeExtension(source.getExtendName()));
    }

    @Override
    public AiKnowledgeParsedDocument parse(AiKnowledgeDocumentSource source) throws BusinessException {
        if (ArrayUtil.isEmpty(source.getFileBytes())) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_SOURCE_EMPTY);
        }

        String extension = normalizeExtension(source.getExtendName());
        return switch (extension) {
            case "pdf" -> buildParsedDocument(source, parsePdfSections(source.getFileBytes()));
            case "doc" -> buildParsedDocument(source, parseDocSections(source.getFileBytes()));
            case "docx" -> buildParsedDocument(source, parseDocxSections(source.getFileBytes()));
            default -> throw new BusinessException(AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE);
        };
    }

    private AiKnowledgeParsedDocument buildParsedDocument(AiKnowledgeDocumentSource source,
                                                          List<AiKnowledgeParsedSection> sections) {
        String content = sections.stream()
                .map(AiKnowledgeParsedSection::getContent)
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.joining("\n\n"));
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
                .sections(sections)
                .build();
    }

    private List<AiKnowledgeParsedSection> parsePdfSections(byte[] fileBytes) {
        try (PDDocument document = Loader.loadPDF(fileBytes)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            List<AiKnowledgeParsedSection> sections = new ArrayList<>();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                textStripper.setStartPage(page);
                textStripper.setEndPage(page);
                String pageText = normalizeText(textStripper.getText(document));
                if (StrUtil.isBlank(pageText)) {
                    continue;
                }
                sections.add(AiKnowledgeParsedSection.builder()
                        .content(pageText)
                        .sourcePage(page)
                        .sourcePosition("page:" + page)
                        .build());
            }
            return ensureSections(sections);
        } catch (IOException e) {
            throw new BusinessException(
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getValue(),
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getLabel() + "：PDF解析失败");
        }
    }

    private List<AiKnowledgeParsedSection> parseDocSections(byte[] fileBytes) {
        try (HWPFDocument document = new HWPFDocument(new ByteArrayInputStream(fileBytes));
             WordExtractor extractor = new WordExtractor(document)) {
            return paragraphsToSections(List.of(extractor.getParagraphText()));
        } catch (IOException e) {
            throw new BusinessException(
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getValue(),
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getLabel() + "：DOC解析失败");
        }
    }

    private List<AiKnowledgeParsedSection> parseDocxSections(byte[] fileBytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(fileBytes))) {
            List<String> paragraphs = document.getParagraphs().stream()
                    .map(XWPFParagraph::getText)
                    .toList();
            return paragraphsToSections(paragraphs);
        } catch (IOException e) {
            throw new BusinessException(
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getValue(),
                    AiErrorEnum.UNSUPPORTED_KNOWLEDGE_DOCUMENT_PARSE_TYPE.getLabel() + "：DOCX解析失败");
        }
    }

    private List<AiKnowledgeParsedSection> paragraphsToSections(List<String> paragraphs) {
        List<AiKnowledgeParsedSection> sections = new ArrayList<>();
        for (String paragraph : paragraphs) {
            String normalized = normalizeText(paragraph);
            if (StrUtil.isBlank(normalized)) {
                continue;
            }
            sections.add(AiKnowledgeParsedSection.builder()
                    .content(normalized)
                    .sourcePosition("paragraph:" + (sections.size() + 1))
                    .build());
        }
        return ensureSections(sections);
    }

    private List<AiKnowledgeParsedSection> ensureSections(List<AiKnowledgeParsedSection> sections) {
        if (CollUtil.isEmpty(sections)) {
            throw new BusinessException(AiErrorEnum.KNOWLEDGE_DOCUMENT_PARSE_CONTENT_EMPTY);
        }
        return sections;
    }

    private String normalizeText(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("\r\n", "\n")
                .replace('\r', '\n')
                .replaceAll("[ \\t]+\\n", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private String normalizeExtension(String extension) {
        return StrUtil.blankToDefault(extension, StrUtil.EMPTY).toLowerCase(Locale.ROOT);
    }
}
