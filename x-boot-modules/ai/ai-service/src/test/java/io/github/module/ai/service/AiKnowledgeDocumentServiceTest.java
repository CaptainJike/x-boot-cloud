package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentChunkMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;
import io.github.module.oss.facade.OssFileInfoFacade;
import io.github.module.oss.model.response.OssFileInfoBO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeDocumentServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant mapperBuilderAssistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiKnowledgeBaseEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiKnowledgeDocumentEntity.class);
        TableInfoHelper.initTableInfo(mapperBuilderAssistant, AiKnowledgeDocumentChunkEntity.class);
    }

    @Mock
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Mock
    private AiKnowledgeDocumentChunkMapper aiKnowledgeDocumentChunkMapper;

    @Mock
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Mock
    private OssFileInfoFacade ossFileInfoFacade;

    @Mock
    private AiKnowledgeDocumentIndexService aiKnowledgeDocumentIndexService;

    @InjectMocks
    private AiKnowledgeDocumentService aiKnowledgeDocumentService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(aiKnowledgeDocumentService, "ossFileInfoFacade", ossFileInfoFacade);
    }

    @Test
    void adminListReturnsStatusAndFailureReason() {
        AiKnowledgeDocumentEntity entity = failedDocument();
        entity.setId(1L);
        Page<AiKnowledgeDocumentEntity> entityPage = new Page<AiKnowledgeDocumentEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiKnowledgeDocumentMapper.selectPage(any(), any())).thenReturn(entityPage);
        when(aiKnowledgeBaseMapper.selectBatchIds(any()))
                .thenReturn(List.of(knowledgeBase()));

        PageResult<AiKnowledgeDocumentBO> result = aiKnowledgeDocumentService.adminList(
                new PageParam(),
                AdminListAiKnowledgeDocumentDTO.builder().knowledgeBaseId(1L).build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getDocumentName()).isEqualTo("制度文档.pdf");
        assertThat(result.getRecords().getFirst().getKnowledgeBaseName()).isEqualTo("企业知识库");
        assertThat(result.getRecords().getFirst().getParseStatus()).isZero();
        assertThat(result.getRecords().getFirst().getErrorMessage()).isEqualTo("PDF解析失败");
    }

    @Test
    void adminBindOssFileSnapshotsMetadataAndInitialStatus() {
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(knowledgeBase());
        when(aiKnowledgeDocumentMapper.selectOne(any())).thenReturn(null);
        when(ossFileInfoFacade.getOneById(9L, true)).thenReturn(ossFileInfo());
        when(aiKnowledgeDocumentMapper.insert(any(AiKnowledgeDocumentEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeDocumentEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });
        when(aiKnowledgeDocumentMapper.selectCount(any())).thenReturn(1L);
        when(aiKnowledgeDocumentMapper.selectList(any())).thenReturn(List.of(new AiKnowledgeDocumentEntity().setChunkCount(0)));

        Long id = aiKnowledgeDocumentService.adminBindOssFile(AdminBindAiKnowledgeDocumentDTO.builder()
                .knowledgeBaseId(1L)
                .ossFileId(9L)
                .documentName(" ")
                .description(" 企业制度 ")
                .autoParse(true)
                .build());

        assertThat(id).isEqualTo(100L);
        ArgumentCaptor<AiKnowledgeDocumentEntity> entityCaptor =
                ArgumentCaptor.forClass(AiKnowledgeDocumentEntity.class);
        verify(aiKnowledgeDocumentMapper).insert(entityCaptor.capture());
        AiKnowledgeDocumentEntity entity = entityCaptor.getValue();
        assertThat(entity.getKnowledgeBaseId()).isEqualTo(1L);
        assertThat(entity.getOssFileId()).isEqualTo(9L);
        assertThat(entity.getDocumentName()).isEqualTo("制度文档.pdf");
        assertThat(entity.getDescription()).isEqualTo("企业制度");
        assertThat(entity.getOriginalFilename()).isEqualTo("制度文档.pdf");
        assertThat(entity.getExtendName()).isEqualTo("pdf");
        assertThat(entity.getFileSize()).isEqualTo(1024L);
        assertThat(entity.getMd5()).isEqualTo("abc");
        assertThat(entity.getStoragePlatform()).isEqualTo("local-plus");
        assertThat(entity.getParseStatus()).isEqualTo(3);
        assertThat(entity.getChunkStatus()).isEqualTo(3);
        assertThat(entity.getEmbeddingStatus()).isEqualTo(3);
        assertThat(entity.getStatus()).isEqualTo(EnabledStatusEnum.ENABLED.getValue());
        assertThat(entity.getChunkCount()).isZero();
        verify(aiKnowledgeBaseMapper).updateById(any(AiKnowledgeBaseEntity.class));
        verify(aiKnowledgeDocumentIndexService).indexDocument(100L);
    }

    @Test
    void adminBindOssFileKeepsCustomNameAndAggregatesKnowledgeBaseStats() {
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(knowledgeBase());
        when(aiKnowledgeDocumentMapper.selectOne(any())).thenReturn(null);
        when(ossFileInfoFacade.getOneById(9L, true)).thenReturn(ossFileInfo());
        when(aiKnowledgeDocumentMapper.insert(any(AiKnowledgeDocumentEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeDocumentEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return 1;
        });
        when(aiKnowledgeDocumentMapper.selectCount(any())).thenReturn(2L);
        when(aiKnowledgeDocumentMapper.selectList(any())).thenReturn(List.of(
                new AiKnowledgeDocumentEntity().setChunkCount(3),
                new AiKnowledgeDocumentEntity().setChunkCount(null),
                new AiKnowledgeDocumentEntity().setChunkCount(7)
        ));

        Long id = aiKnowledgeDocumentService.adminBindOssFile(AdminBindAiKnowledgeDocumentDTO.builder()
                .knowledgeBaseId(1L)
                .ossFileId(9L)
                .documentName(" 员工 手册 ")
                .description(" ")
                .autoParse(false)
                .build());

        assertThat(id).isEqualTo(101L);
        ArgumentCaptor<AiKnowledgeDocumentEntity> documentCaptor =
                ArgumentCaptor.forClass(AiKnowledgeDocumentEntity.class);
        verify(aiKnowledgeDocumentMapper).insert(documentCaptor.capture());
        assertThat(documentCaptor.getValue().getDocumentName()).isEqualTo("员工手册");
        assertThat(documentCaptor.getValue().getDescription()).isEmpty();
        assertThat(documentCaptor.getValue().getRetryCount()).isZero();
        ArgumentCaptor<AiKnowledgeBaseEntity> knowledgeBaseCaptor =
                ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).updateById(knowledgeBaseCaptor.capture());
        assertThat(knowledgeBaseCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(knowledgeBaseCaptor.getValue().getDocumentCount()).isEqualTo(2);
        assertThat(knowledgeBaseCaptor.getValue().getChunkCount()).isEqualTo(10);
        verify(aiKnowledgeDocumentIndexService).indexDocument(101L);
    }

    @Test
    void adminBindOssFileRejectsDuplicateBinding() {
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(knowledgeBase());
        AiKnowledgeDocumentEntity existing = new AiKnowledgeDocumentEntity();
        existing.setId(2L);
        when(aiKnowledgeDocumentMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> aiKnowledgeDocumentService.adminBindOssFile(AdminBindAiKnowledgeDocumentDTO.builder()
                .knowledgeBaseId(1L)
                .ossFileId(9L)
                .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已存在相同知识库文档，请勿重复关联");
        verify(ossFileInfoFacade, never()).getOneById(any(), anyBoolean());
        verify(aiKnowledgeDocumentMapper, never()).insert(any(AiKnowledgeDocumentEntity.class));
    }

    @Test
    void getOneByIdReturnsFailureDetails() {
        AiKnowledgeDocumentEntity entity = failedDocument();
        entity.setId(1L);
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(entity);
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(knowledgeBase());

        AiKnowledgeDocumentDetailBO result = aiKnowledgeDocumentService.getOneById(1L, true);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getKnowledgeBaseName()).isEqualTo("企业知识库");
        assertThat(result.getParseErrorMessage()).isEqualTo("PDF解析失败");
        assertThat(result.getChunkErrorMessage()).isEqualTo("切片失败");
        assertThat(result.getRetryCount()).isEqualTo(2);
    }

    @Test
    void adminRetryResetsStatusesAndIncrementsRetryCount() {
        AiKnowledgeDocumentEntity existing = failedDocument();
        existing.setId(1L);
        existing.setRetryCount(2);
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(existing);

        aiKnowledgeDocumentService.adminRetry(1L);

        ArgumentCaptor<AiKnowledgeDocumentEntity> entityCaptor =
                ArgumentCaptor.forClass(AiKnowledgeDocumentEntity.class);
        verify(aiKnowledgeDocumentMapper).updateById(entityCaptor.capture());
        AiKnowledgeDocumentEntity entity = entityCaptor.getValue();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getParseStatus()).isEqualTo(3);
        assertThat(entity.getChunkStatus()).isEqualTo(3);
        assertThat(entity.getEmbeddingStatus()).isEqualTo(3);
        assertThat(entity.getParseErrorMessage()).isEmpty();
        assertThat(entity.getChunkErrorMessage()).isEmpty();
        assertThat(entity.getRetryCount()).isEqualTo(3);
        assertThat(entity.getLastRetryAt()).isNotNull();
        verify(aiKnowledgeDocumentIndexService).indexDocument(1L);
    }

    @Test
    void adminListChunksReturnsDocumentName() {
        AiKnowledgeDocumentEntity document = failedDocument();
        document.setId(1L);
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document);
        Page<AiKnowledgeDocumentChunkEntity> entityPage = new Page<AiKnowledgeDocumentChunkEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(new AiKnowledgeDocumentChunkEntity()
                        .setKnowledgeBaseId(1L)
                        .setDocumentId(1L)
                        .setChunkNo(1)
                        .setContent("制度内容")
                        .setContentPreview("制度内容")
                        .setSourcePage(3)
                        .setSourcePosition("p3")
                        .setTokenCount(12)
                        .setStatus(1)
                        .setEmbeddingStatus(3)));
        when(aiKnowledgeDocumentChunkMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AiKnowledgeDocumentChunkBO> result = aiKnowledgeDocumentService.adminListChunks(
                1L,
                new PageParam(),
                AdminListAiKnowledgeDocumentChunkDTO.builder().keyword("制度").build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords().getFirst().getDocumentName()).isEqualTo("制度文档.pdf");
        assertThat(result.getRecords().getFirst().getChunkNo()).isEqualTo(1);
        assertThat(result.getRecords().getFirst().getContentPreview()).isEqualTo("制度内容");
        assertThat(result.getRecords().getFirst().getSourcePage()).isEqualTo(3);
        assertThat(result.getRecords().getFirst().getSourcePosition()).isEqualTo("p3");
        assertThat(result.getRecords().getFirst().getTokenCount()).isEqualTo(12);
        assertThat(result.getRecords().getFirst().getEmbeddingStatus()).isEqualTo(3);
    }

    @Test
    void adminDeleteRemovesDocumentsAndChunksThenRefreshesStats() {
        when(aiKnowledgeDocumentMapper.selectBatchIds(List.of(1L, 2L)))
                .thenReturn(List.of(
                        documentWithId(1L, 1L),
                        documentWithId(2L, 1L)
                ));
        when(aiKnowledgeDocumentMapper.selectCount(any())).thenReturn(0L);
        when(aiKnowledgeDocumentMapper.selectList(any())).thenReturn(List.of());

        aiKnowledgeDocumentService.adminDelete(List.of(1L, 2L));

        verify(aiKnowledgeDocumentChunkMapper).delete(any());
        verify(aiKnowledgeDocumentMapper).deleteBatchIds(List.of(1L, 2L));
        ArgumentCaptor<AiKnowledgeBaseEntity> entityCaptor = ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(entityCaptor.getValue().getDocumentCount()).isZero();
        assertThat(entityCaptor.getValue().getChunkCount()).isZero();
        verify(aiKnowledgeDocumentIndexService).deleteDocumentVectors(1L);
        verify(aiKnowledgeDocumentIndexService).deleteDocumentVectors(2L);
    }

    private AiKnowledgeDocumentEntity documentWithId(Long id, Long knowledgeBaseId) {
        AiKnowledgeDocumentEntity entity = new AiKnowledgeDocumentEntity()
                .setKnowledgeBaseId(knowledgeBaseId);
        entity.setId(id);
        return entity;
    }

    private AiKnowledgeDocumentEntity failedDocument() {
        return new AiKnowledgeDocumentEntity()
                .setKnowledgeBaseId(1L)
                .setOssFileId(9L)
                .setDocumentName("制度文档.pdf")
                .setDescription("企业制度")
                .setOriginalFilename("制度文档.pdf")
                .setExtendName("pdf")
                .setFileSize(1024L)
                .setMd5("abc")
                .setStoragePlatform("local-plus")
                .setParseStatus(0)
                .setChunkStatus(0)
                .setEmbeddingStatus(3)
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setChunkCount(0)
                .setParseErrorMessage("PDF解析失败")
                .setChunkErrorMessage("切片失败")
                .setRetryCount(2);
    }

    private AiKnowledgeBaseEntity knowledgeBase() {
        AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity()
                .setName("企业知识库");
        entity.setId(1L);
        return entity;
    }

    private OssFileInfoBO ossFileInfo() {
        return OssFileInfoBO.builder()
                .id(9L)
                .originalFilename("制度文档")
                .extendName("pdf")
                .fileSize(1024L)
                .md5("abc")
                .storagePlatform("local-plus")
                .build();
    }
}
