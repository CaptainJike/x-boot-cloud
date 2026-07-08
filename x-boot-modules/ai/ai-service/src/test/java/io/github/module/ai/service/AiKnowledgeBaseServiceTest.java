package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeBaseServiceTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                AiKnowledgeBaseEntity.class
        );
    }

    @Mock
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @InjectMocks
    private AiKnowledgeBaseService aiKnowledgeBaseService;

    @Test
    void adminListReturnsPagedKnowledgeBases() {
        AiKnowledgeBaseEntity entity = enabledEntity();
        entity.setId(1L);
        Page<AiKnowledgeBaseEntity> entityPage = new Page<AiKnowledgeBaseEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiKnowledgeBaseMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AiKnowledgeBaseBO> result = aiKnowledgeBaseService.adminList(
                new PageParam(),
                AdminListAiKnowledgeBaseDTO.builder().name("企业").build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getName()).isEqualTo("企业知识库");
        assertThat(result.getRecords().getFirst().getDocumentCount()).isEqualTo(2);
    }

    @Test
    void adminInsertRejectsDuplicateName() {
        AiKnowledgeBaseEntity existing = enabledEntity();
        existing.setId(2L);
        when(aiKnowledgeBaseMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> aiKnowledgeBaseService.adminInsert(validDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已存在相同知识库，请重新输入");
        verify(aiKnowledgeBaseMapper, never()).insert(any(AiKnowledgeBaseEntity.class));
    }

    @Test
    void adminInsertNormalizesDefaultsAndSnapshotsEmbeddingConfig() {
        when(aiKnowledgeBaseMapper.selectOne(any())).thenReturn(null);
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(AiModelConfigBO.builder()
                        .id(9L)
                        .code("embedding-default")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("text-embedding-v1")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build());
        when(aiKnowledgeBaseMapper.insert(any(AiKnowledgeBaseEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeBaseEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        Long id = aiKnowledgeBaseService.adminInsert(validDto()
                .setName(" 企业 知识库 ")
                .setDescription(" ")
                .setRetrievalTopK(null)
                .setSimilarityThreshold(null));

        assertThat(id).isEqualTo(1L);
        ArgumentCaptor<AiKnowledgeBaseEntity> entityCaptor = ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).insert(entityCaptor.capture());
        AiKnowledgeBaseEntity entity = entityCaptor.getValue();
        assertThat(entity.getName()).isEqualTo("企业知识库");
        assertThat(entity.getDescription()).isEmpty();
        assertThat(entity.getRetrievalTopK()).isEqualTo(5);
        assertThat(entity.getSimilarityThreshold()).isEqualTo(0.0D);
        assertThat(entity.getEmbeddingModelConfigId()).isEqualTo(9L);
        assertThat(entity.getEmbeddingProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(entity.getEmbeddingModelName()).isEqualTo("text-embedding-v1");
        assertThat(entity.getDocumentCount()).isZero();
        assertThat(entity.getChunkCount()).isZero();
    }

    @Test
    void adminUpdateAllowsSameNameAndRefreshesEmbeddingSnapshot() {
        AiKnowledgeBaseEntity existing = enabledEntity();
        existing.setId(1L);
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(existing);
        when(aiKnowledgeBaseMapper.selectOne(any())).thenReturn(existing);
        when(aiModelConfigService.getEnabledConfigByCode("embedding-new", true))
                .thenReturn(AiModelConfigBO.builder()
                        .id(10L)
                        .code("embedding-new")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("text-embedding-v2")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build());

        aiKnowledgeBaseService.adminUpdate(validDto()
                .setId(1L)
                .setName(" 企业 知识库 ")
                .setDescription(" ")
                .setEmbeddingModelConfigCode(" embedding-new ")
                .setRetrievalTopK(null)
                .setSimilarityThreshold(null));

        ArgumentCaptor<AiKnowledgeBaseEntity> entityCaptor = ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).updateById(entityCaptor.capture());
        AiKnowledgeBaseEntity entity = entityCaptor.getValue();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getName()).isEqualTo("企业知识库");
        assertThat(entity.getDescription()).isEmpty();
        assertThat(entity.getRetrievalTopK()).isEqualTo(5);
        assertThat(entity.getSimilarityThreshold()).isEqualTo(0.0D);
        assertThat(entity.getEmbeddingModelConfigId()).isEqualTo(10L);
        assertThat(entity.getEmbeddingModelConfigCode()).isEqualTo("embedding-new");
        assertThat(entity.getEmbeddingProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(entity.getEmbeddingModelName()).isEqualTo("text-embedding-v2");
    }

    @Test
    void adminInsertRejectsNonEmbeddingModelConfig() {
        when(aiKnowledgeBaseMapper.selectOne(any())).thenReturn(null);
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(AiModelConfigBO.builder()
                        .id(9L)
                        .code("embedding-default")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("qwen-plus")
                        .supportedCapabilities(AiModelCapabilityConstant.CHAT)
                        .build());

        assertThatThrownBy(() -> aiKnowledgeBaseService.adminInsert(validDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效知识库向量化配置");

        verify(aiKnowledgeBaseMapper, never()).insert(any(AiKnowledgeBaseEntity.class));
    }

    @Test
    void getOneByIdReturnsDetail() {
        AiKnowledgeBaseEntity entity = enabledEntity();
        entity.setId(1L);
        entity.setLastParsedAt(LocalDateTime.of(2026, 6, 21, 10, 0));
        entity.setLastRetrievedAt(LocalDateTime.of(2026, 6, 21, 11, 0));
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(entity);

        AiKnowledgeBaseDetailBO result = aiKnowledgeBaseService.getOneById(1L, true);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("企业知识库");
        assertThat(result.getLastParsedAt()).isEqualTo(LocalDateTime.of(2026, 6, 21, 10, 0));
        assertThat(result.getLastRetrievedAt()).isEqualTo(LocalDateTime.of(2026, 6, 21, 11, 0));
    }

    @Test
    void adminUpdateStatusRejectsInvalidStatus() {
        assertThatThrownBy(() -> aiKnowledgeBaseService.adminUpdateStatus(1L, 9))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效知识库状态");
        verify(aiKnowledgeBaseMapper, never()).selectById(any());
        verify(aiKnowledgeBaseMapper, never()).updateById(any(AiKnowledgeBaseEntity.class));
    }

    @Test
    void adminUpdateStatusUpdatesExistingKnowledgeBase() {
        AiKnowledgeBaseEntity existing = enabledEntity();
        existing.setId(1L);
        when(aiKnowledgeBaseMapper.selectById(1L)).thenReturn(existing);

        aiKnowledgeBaseService.adminUpdateStatus(1L, EnabledStatusEnum.DISABLED.getValue());

        ArgumentCaptor<AiKnowledgeBaseEntity> entityCaptor = ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(entityCaptor.getValue().getStatus()).isEqualTo(EnabledStatusEnum.DISABLED.getValue());
    }

    @Test
    void adminSelectOptionsReturnsEnabledKnowledgeBases() {
        AiKnowledgeBaseEntity entity = enabledEntity();
        entity.setId(1L);
        when(aiKnowledgeBaseMapper.selectList(any())).thenReturn(List.of(entity));

        List<AiKnowledgeBaseBO> result = aiKnowledgeBaseService.adminSelectOptions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("企业知识库");
    }

    private AdminInsertOrUpdateAiKnowledgeBaseDTO validDto() {
        return AdminInsertOrUpdateAiKnowledgeBaseDTO.builder()
                .name("企业知识库")
                .description("企业内部知识")
                .embeddingModelConfigCode("embedding-default")
                .retrievalTopK(5)
                .similarityThreshold(0.5D)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .build();
    }

    private AiKnowledgeBaseEntity enabledEntity() {
        return new AiKnowledgeBaseEntity()
                .setName("企业知识库")
                .setDescription("企业内部知识")
                .setEmbeddingModelConfigId(9L)
                .setEmbeddingModelConfigCode("embedding-default")
                .setEmbeddingProviderType("OPENAI_COMPATIBLE")
                .setEmbeddingModelName("text-embedding-v1")
                .setRetrievalTopK(5)
                .setSimilarityThreshold(0.5D)
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setDocumentCount(2)
                .setChunkCount(16);
    }
}
