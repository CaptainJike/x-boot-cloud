package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.oss.facade.OssUploadDownloadFacade;
import io.github.module.oss.model.response.OssFileDownloadReplyBO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeDocumentSourceServiceTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                AiKnowledgeDocumentEntity.class
        );
    }

    @Mock
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Mock
    private OssUploadDownloadFacade ossUploadDownloadFacade;

    @InjectMocks
    private AiKnowledgeDocumentSourceService aiKnowledgeDocumentSourceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiKnowledgeDocumentSourceService,
                "ossUploadDownloadFacade",
                ossUploadDownloadFacade);
    }

    @Test
    void loadSourceDownloadsProxyBytesFromOssFile() {
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document());
        when(ossUploadDownloadFacade.downloadById(9L)).thenReturn(OssFileDownloadReplyBO.builder()
                .redirect2DirectUrl(false)
                .fileBytes("hello".getBytes(StandardCharsets.UTF_8))
                .storageFilename("policy.pdf")
                .build());

        AiKnowledgeDocumentSource source = aiKnowledgeDocumentSourceService.loadSource(1L);

        assertThat(source.getDocumentId()).isEqualTo(1L);
        assertThat(source.getKnowledgeBaseId()).isEqualTo(2L);
        assertThat(source.getOssFileId()).isEqualTo(9L);
        assertThat(source.getDocumentName()).isEqualTo("制度文档.pdf");
        assertThat(source.getOriginalFilename()).isEqualTo("制度文档.pdf");
        assertThat(source.getExtendName()).isEqualTo("pdf");
        assertThat(source.getFileSize()).isEqualTo(1024L);
        assertThat(source.getMd5()).isEqualTo("abc");
        assertThat(source.getStoragePlatform()).isEqualTo("local-plus");
        assertThat(source.getStorageFilename()).isEqualTo("policy.pdf");
        assertThat(source.isDirectUrlSource()).isFalse();
        assertThat(source.getFileBytes()).isEqualTo("hello".getBytes(StandardCharsets.UTF_8));
        verify(ossUploadDownloadFacade).downloadById(9L);
    }

    @Test
    void loadSourceSupportsDirectUrlSource() {
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document());
        when(ossUploadDownloadFacade.downloadById(9L)).thenReturn(OssFileDownloadReplyBO.builder()
                .redirect2DirectUrl(true)
                .directUrl("https://oss.example.com/policy.pdf")
                .storageFilename("policy.pdf")
                .build());

        AiKnowledgeDocumentSource source = aiKnowledgeDocumentSourceService.loadSource(1L);

        assertThat(source.isDirectUrlSource()).isTrue();
        assertThat(source.getDirectUrl()).isEqualTo("https://oss.example.com/policy.pdf");
        assertThat(source.getFileBytes()).isNull();
    }

    @Test
    void loadSourceRejectsInvalidDocumentId() {
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> aiKnowledgeDocumentSourceService.loadSource(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效ID");
    }

    @Test
    void loadSourceRejectsEmptyProxyDownloadBytes() {
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document());
        when(ossUploadDownloadFacade.downloadById(9L)).thenReturn(OssFileDownloadReplyBO.builder()
                .redirect2DirectUrl(false)
                .fileBytes(new byte[0])
                .storageFilename("policy.pdf")
                .build());

        assertThatThrownBy(() -> aiKnowledgeDocumentSourceService.loadSource(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库文档源文件为空");
    }

    @Test
    void loadSourceRejectsDirectUrlWithoutUrl() {
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document());
        when(ossUploadDownloadFacade.downloadById(9L)).thenReturn(OssFileDownloadReplyBO.builder()
                .redirect2DirectUrl(true)
                .storageFilename("policy.pdf")
                .build());

        assertThatThrownBy(() -> aiKnowledgeDocumentSourceService.loadSource(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("知识库文档源文件不可用");
    }

    private AiKnowledgeDocumentEntity document() {
        AiKnowledgeDocumentEntity entity = new AiKnowledgeDocumentEntity()
                .setKnowledgeBaseId(2L)
                .setOssFileId(9L)
                .setDocumentName("制度文档.pdf")
                .setOriginalFilename("制度文档.pdf")
                .setExtendName("pdf")
                .setFileSize(1024L)
                .setMd5("abc")
                .setStoragePlatform("local-plus");
        entity.setId(1L);
        return entity;
    }
}
