package io.github.module.ai.service.strategy;

import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeParsedDocument;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class OfficeAiKnowledgeDocumentParseStrategyTest {

    private final OfficeAiKnowledgeDocumentParseStrategy strategy = new OfficeAiKnowledgeDocumentParseStrategy();

    @Test
    void supportsPdfAndWordExtensions() {
        assertThat(strategy.supports(source("pdf", new byte[]{1}))).isTrue();
        assertThat(strategy.supports(source("doc", new byte[]{1}))).isTrue();
        assertThat(strategy.supports(source("docx", new byte[]{1}))).isTrue();
        assertThat(strategy.supports(source("md", new byte[]{1}))).isFalse();
    }

    @Test
    void parsePdfExtractsPageText() throws IOException {
        AiKnowledgeParsedDocument document = strategy.parse(source("pdf", createPdf("Policy page one")));

        assertThat(document.getContent()).contains("Policy page one");
        assertThat(document.getSections()).hasSize(1);
        assertThat(document.getSections().getFirst().getSourcePage()).isEqualTo(1);
        assertThat(document.getSections().getFirst().getSourcePosition()).isEqualTo("page:1");
    }

    @Test
    void parseDocxExtractsParagraphs() throws IOException {
        AiKnowledgeParsedDocument document = strategy.parse(source("docx", createDocx("第一段制度", "第二段流程")));

        assertThat(document.getContent()).contains("第一段制度").contains("第二段流程");
        assertThat(document.getSections()).hasSize(2);
        assertThat(document.getSections().getFirst().getSourcePosition()).isEqualTo("paragraph:1");
        assertThat(document.getSections().get(1).getSourcePosition()).isEqualTo("paragraph:2");
    }

    private AiKnowledgeDocumentSource source(String extension, byte[] fileBytes) {
        return AiKnowledgeDocumentSource.builder()
                .documentId(1L)
                .knowledgeBaseId(2L)
                .ossFileId(3L)
                .documentName("知识库文档")
                .originalFilename("知识库文档." + extension)
                .extendName(extension)
                .fileBytes(fileBytes)
                .build();
    }

    private byte[] createPdf(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createDocx(String... paragraphs) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String content : paragraphs) {
                XWPFParagraph paragraph = document.createParagraph();
                paragraph.createRun().setText(content);
            }
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
