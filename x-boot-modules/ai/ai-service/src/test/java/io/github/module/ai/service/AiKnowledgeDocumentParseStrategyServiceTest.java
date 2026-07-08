package io.github.module.ai.service;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkConfig;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import io.github.module.ai.service.model.AiKnowledgeParsedSection;
import io.github.module.ai.service.strategy.DefaultAiKnowledgeDocumentChunkStrategy;
import io.github.module.ai.service.strategy.PlainTextAiKnowledgeDocumentParseStrategy;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiKnowledgeDocumentParseStrategyServiceTest {

    private final PlainTextAiKnowledgeDocumentParseStrategy plainTextStrategy =
            new PlainTextAiKnowledgeDocumentParseStrategy();

    private final DefaultAiKnowledgeDocumentChunkStrategy chunkStrategy =
            new DefaultAiKnowledgeDocumentChunkStrategy();

    @Test
    void plainTextStrategyParsesMarkdownSections() {
        String markdown = "# 入职制度\n第一段内容\n\n## 休假规则\n第二段内容";

        AiKnowledgeParsedDocument parsed = plainTextStrategy.parse(source("md", markdown));

        assertThat(parsed.getDocumentId()).isEqualTo(1L);
        assertThat(parsed.getKnowledgeBaseId()).isEqualTo(2L);
        assertThat(parsed.getOssFileId()).isEqualTo(9L);
        assertThat(parsed.getContent()).isEqualTo(markdown);
        assertThat(parsed.getSections()).hasSize(2);
        assertThat(parsed.getSections())
                .extracting(AiKnowledgeParsedSection::getTitle)
                .containsExactly("入职制度", "休假规则");
        assertThat(parsed.getSections())
                .extracting(AiKnowledgeParsedSection::getSourcePosition)
                .containsExactly("paragraph:1", "paragraph:2");
    }

    @Test
    void plainTextStrategyRejectsBlankContent() {
        assertThatThrownBy(() -> plainTextStrategy.parse(source("txt", "   \n\t  ")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库文档解析内容为空");
    }

    @Test
    void parseStrategyServiceRejectsUnsupportedExtension() {
        AiKnowledgeDocumentParseStrategyService service = new AiKnowledgeDocumentParseStrategyService(
                List.of(plainTextStrategy),
                chunkStrategy
        );

        assertThatThrownBy(() -> service.parse(source("pdf", "PDF bytes")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("不支持的知识库文档解析类型");
    }

    @Test
    void parseAndChunkSplitsWithOverlapAndMetadata() {
        AiKnowledgeDocumentParseStrategyService service = new AiKnowledgeDocumentParseStrategyService(
                List.of(plainTextStrategy),
                chunkStrategy
        );
        AiKnowledgeDocumentChunkConfig config = AiKnowledgeDocumentChunkConfig.builder()
                .maxTokens(5)
                .overlapTokens(1)
                .tokenCharRatio(2)
                .build();

        List<AiKnowledgeDocumentChunkDraft> drafts = service.parseAndChunk(
                source("txt", "0123456789ABCDEFGHIJ"),
                config
        );

        assertThat(drafts).hasSize(2);
        assertThat(drafts.getFirst().getChunkNo()).isEqualTo(1);
        assertThat(drafts.getFirst().getContent()).isEqualTo("0123456789");
        assertThat(drafts.getFirst().getContentPreview()).isEqualTo("0123456789");
        assertThat(drafts.getFirst().getKnowledgeBaseId()).isEqualTo(2L);
        assertThat(drafts.getFirst().getDocumentId()).isEqualTo(1L);
        assertThat(drafts.getFirst().getTokenCount()).isEqualTo(5);
        assertThat(drafts.get(1).getChunkNo()).isEqualTo(2);
        assertThat(drafts.get(1).getContent()).isEqualTo("89ABCDEFGHIJ");
        assertThat(drafts.get(1).getSourcePosition()).isEqualTo("paragraph:1");
        assertThat(drafts.get(1).getTokenCount()).isEqualTo(6);
    }

    @Test
    void chunkStrategyFallsBackToDocumentContentWhenSectionsEmpty() {
        AiKnowledgeParsedDocument document = AiKnowledgeParsedDocument.builder()
                .documentId(1L)
                .knowledgeBaseId(2L)
                .content("整篇文档内容")
                .sections(List.of())
                .build();

        List<AiKnowledgeDocumentChunkDraft> drafts = chunkStrategy.chunk(document, null);

        assertThat(drafts).hasSize(1);
        assertThat(drafts.getFirst().getChunkNo()).isEqualTo(1);
        assertThat(drafts.getFirst().getContent()).isEqualTo("整篇文档内容");
        assertThat(drafts.getFirst().getSourcePosition()).isEqualTo("paragraph:1");
        assertThat(drafts.getFirst().getKnowledgeBaseId()).isEqualTo(2L);
        assertThat(drafts.getFirst().getDocumentId()).isEqualTo(1L);
        assertThat(drafts.getFirst().getTokenCount()).isPositive();
    }

    @Test
    void chunkStrategyRejectsInvalidConfig() {
        AiKnowledgeParsedDocument document = AiKnowledgeParsedDocument.builder()
                .documentId(1L)
                .knowledgeBaseId(2L)
                .content("制度内容")
                .sections(List.of(AiKnowledgeParsedSection.builder()
                        .content("制度内容")
                        .sourcePosition("paragraph:1")
                        .build()))
                .build();
        AiKnowledgeDocumentChunkConfig config = AiKnowledgeDocumentChunkConfig.builder()
                .maxTokens(10)
                .overlapTokens(10)
                .tokenCharRatio(2)
                .build();

        assertThatThrownBy(() -> chunkStrategy.chunk(document, config))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效知识库文档切片配置");
    }

    private AiKnowledgeDocumentSource source(String extendName, String content) {
        return AiKnowledgeDocumentSource.builder()
                .documentId(1L)
                .knowledgeBaseId(2L)
                .ossFileId(9L)
                .documentName("制度文档." + extendName)
                .originalFilename("制度文档." + extendName)
                .extendName(extendName)
                .fileBytes(content.getBytes(StandardCharsets.UTF_8))
                .build();
    }
}
