package io.github.module.ai.service;

import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.model.request.AdminAiChatAttachmentDTO;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.oss.facade.OssFileInfoFacade;
import io.github.module.oss.facade.OssUploadDownloadFacade;
import io.github.module.oss.model.response.OssFileDownloadReplyBO;
import io.github.module.oss.model.response.OssFileInfoBO;
import io.github.starter.ai.vo.AiChatMedia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatAttachmentServiceTest {

    @Mock
    private OssFileInfoFacade ossFileInfoFacade;

    @Mock
    private OssUploadDownloadFacade ossUploadDownloadFacade;

    private AiChatAttachmentService aiChatAttachmentService;

    @BeforeEach
    void setUp() {
        aiChatAttachmentService = new AiChatAttachmentService();
        ReflectionTestUtils.setField(aiChatAttachmentService, "ossFileInfoFacade", ossFileInfoFacade);
        ReflectionTestUtils.setField(aiChatAttachmentService, "ossUploadDownloadFacade", ossUploadDownloadFacade);
    }

    @Test
    void prepareAllowsBlankContentWhenImageAttachmentExists() {
        when(ossFileInfoFacade.getOneById(11L, true)).thenReturn(imageFile());

        AiChatAttachmentService.ChatAttachments attachments = aiChatAttachmentService.prepare(AdminAiChatDTO.builder()
                .content("")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(11L)
                        .attachmentType("image")
                        .build()))
                .build(), visionModel());

        assertThat(attachments.all()).hasSize(1);
        assertThat(attachments.images()).hasSize(1);
        assertThat(attachments.all().getFirst().getAttachmentType()).isEqualTo("image");
        assertThat(attachments.all().getFirst().getMimeType()).isEqualTo("image/png");
        assertThat(attachments.all().getFirst().getFileName()).isEqualTo("chart.png");
    }

    @Test
    void prepareRejectsBlankContentWhenOnlyRegularFileExists() {
        when(ossFileInfoFacade.getOneById(12L, true)).thenReturn(fileInfo("report", "xlsx", 2048L));

        assertThatThrownBy(() -> aiChatAttachmentService.prepare(AdminAiChatDTO.builder()
                .content("")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(12L)
                        .attachmentType("file")
                        .build()))
                .build(), visionModel()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("仅上传普通文件时需要输入对话内容");
    }

    @Test
    void prepareRejectsImageWhenModelDoesNotSupportVision() {
        when(ossFileInfoFacade.getOneById(11L, true)).thenReturn(imageFile());

        assertThatThrownBy(() -> aiChatAttachmentService.prepare(AdminAiChatDTO.builder()
                .content("看图")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(11L)
                        .attachmentType("image")
                        .build()))
                .build(), textModel()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("当前AI模型配置不支持图片理解");
    }

    @Test
    void toImageMediaDownloadsImageBytesFromOss() {
        when(ossFileInfoFacade.getOneById(11L, true)).thenReturn(imageFile());
        when(ossUploadDownloadFacade.downloadById(11L)).thenReturn(OssFileDownloadReplyBO.builder()
                .fileBytes(new byte[]{1, 2, 3})
                .storageFilename("chart.png")
                .build());
        AiChatAttachmentService.ChatAttachments attachments = aiChatAttachmentService.prepare(AdminAiChatDTO.builder()
                .content("看图")
                .attachments(List.of(AdminAiChatAttachmentDTO.builder()
                        .ossFileId(11L)
                        .attachmentType("image")
                        .build()))
                .build(), visionModel());

        List<AiChatMedia> mediaList = aiChatAttachmentService.toImageMedia(attachments);

        assertThat(mediaList).hasSize(1);
        assertThat(mediaList.getFirst().getMimeType()).isEqualTo("image/png");
        assertThat(mediaList.getFirst().getName()).isEqualTo("chart.png");
        assertThat(mediaList.getFirst().getData()).containsExactly(1, 2, 3);
    }

    private OssFileInfoBO imageFile() {
        return fileInfo("chart", "png", 1024L);
    }

    private OssFileInfoBO fileInfo(String originalFilename, String extendName, Long fileSize) {
        return OssFileInfoBO.builder()
                .id("png".equals(extendName) ? 11L : 12L)
                .originalFilename(originalFilename)
                .extendName(extendName)
                .fileSize(fileSize)
                .build();
    }

    private AiModelConfigBO visionModel() {
        return AiModelConfigBO.builder()
                .code("vision")
                .supportedModalities("text,image")
                .build();
    }

    private AiModelConfigBO textModel() {
        return AiModelConfigBO.builder()
                .code("text")
                .supportedModalities("text")
                .build();
    }
}
