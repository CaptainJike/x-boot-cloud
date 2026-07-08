package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentChunkEntity;
import io.github.module.ai.entity.AiKnowledgeDocumentEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentChunkMapper;
import io.github.module.ai.mapper.AiKnowledgeDocumentMapper;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.embedding.AiKnowledgeEmbeddingProviderService;
import io.github.module.ai.service.model.AiKnowledgeDocumentChunkDraft;
import io.github.module.ai.service.model.AiKnowledgeDocumentSource;
import io.github.module.ai.service.model.AiKnowledgeEmbeddingResponse;
import io.github.module.ai.service.model.AiKnowledgeVectorUpsertRequest;
import io.github.module.ai.service.vector.AiKnowledgeVectorStore;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeDocumentIndexServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, AiKnowledgeBaseEntity.class);
        TableInfoHelper.initTableInfo(assistant, AiKnowledgeDocumentEntity.class);
        TableInfoHelper.initTableInfo(assistant, AiKnowledgeDocumentChunkEntity.class);
    }

    @Mock
    private AiKnowledgeDocumentMapper aiKnowledgeDocumentMapper;

    @Mock
    private AiKnowledgeDocumentChunkMapper aiKnowledgeDocumentChunkMapper;

    @Mock
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @Mock
    private AiKnowledgeDocumentSourceService aiKnowledgeDocumentSourceService;

    @Mock
    private AiKnowledgeDocumentParseStrategyService aiKnowledgeDocumentParseStrategyService;

    @Mock
    private AiKnowledgeEmbeddingProviderService embeddingProviderService;

    @Mock
    private AiKnowledgeVectorStore vectorStore;

    @InjectMocks
    private AiKnowledgeDocumentIndexService aiKnowledgeDocumentIndexService;

    @Test
    void indexDocumentParsesEmbedsUpsertsAndWritesSuccessStatus() {
        prepareSuccessfulIndexMocks();

        aiKnowledgeDocumentIndexService.indexDocument(1L);

        verify(vectorStore).deleteByDocumentId(1L);
        ArgumentCaptor<AiKnowledgeVectorUpsertRequest> upsertCaptor =
                ArgumentCaptor.forClass(AiKnowledgeVectorUpsertRequest.class);
        verify(vectorStore).upsert(upsertCaptor.capture());
        AiKnowledgeVectorUpsertRequest upsertRequest = upsertCaptor.getValue();
        assertThat(upsertRequest.getContext().getModelConfigId()).isEqualTo(7L);
        assertThat(upsertRequest.getDocuments()).hasSize(1);
        assertThat(upsertRequest.getDocuments().getFirst().getChunkId()).isEqualTo(101L);
        assertThat(upsertRequest.getDocuments().getFirst().getVector()).containsExactly(0.1D, 0.2D);

        ArgumentCaptor<AiKnowledgeDocumentEntity> documentCaptor =
                ArgumentCaptor.forClass(AiKnowledgeDocumentEntity.class);
        verify(aiKnowledgeDocumentMapper, org.mockito.Mockito.atLeastOnce()).updateById(documentCaptor.capture());
        assertThat(documentCaptor.getAllValues())
                .anySatisfy(entity -> {
                    assertThat(entity.getId()).isEqualTo(1L);
                    assertThat(entity.getParseStatus()).isEqualTo(1);
                    assertThat(entity.getChunkStatus()).isEqualTo(1);
                    assertThat(entity.getEmbeddingStatus()).isEqualTo(1);
                    assertThat(entity.getChunkCount()).isEqualTo(1);
                });
    }

    @Test
    void indexDocumentMarksEmbeddingFailedWhenVectorStoreFails() {
        prepareSuccessfulIndexMocks();
        doThrow(new BusinessException(AiErrorEnum.KNOWLEDGE_VECTOR_STORE_UNAVAILABLE))
                .when(vectorStore)
                .upsert(any(AiKnowledgeVectorUpsertRequest.class));

        aiKnowledgeDocumentIndexService.indexDocument(1L);

        ArgumentCaptor<AiKnowledgeDocumentEntity> documentCaptor =
                ArgumentCaptor.forClass(AiKnowledgeDocumentEntity.class);
        verify(aiKnowledgeDocumentMapper, org.mockito.Mockito.atLeastOnce()).updateById(documentCaptor.capture());
        assertThat(documentCaptor.getAllValues())
                .anySatisfy(entity -> {
                    assertThat(entity.getId()).isEqualTo(1L);
                    assertThat(entity.getParseStatus()).isEqualTo(1);
                    assertThat(entity.getChunkStatus()).isEqualTo(1);
                    assertThat(entity.getEmbeddingStatus()).isEqualTo(0);
                    assertThat(entity.getChunkErrorMessage()).contains("知识库向量存储不可用");
                });
        verify(aiKnowledgeDocumentChunkMapper).update(any(AiKnowledgeDocumentChunkEntity.class), any());
    }

    private void prepareSuccessfulIndexMocks() {
        AiKnowledgeDocumentEntity document = new AiKnowledgeDocumentEntity()
                .setKnowledgeBaseId(11L)
                .setOssFileId(9L)
                .setDocumentName("制度.md");
        document.setId(1L);
        when(aiKnowledgeDocumentMapper.selectById(1L)).thenReturn(document);
        when(aiKnowledgeBaseMapper.selectById(11L)).thenReturn(new AiKnowledgeBaseEntity()
                .setEmbeddingModelConfigCode("emb"));
        when(aiModelConfigService.getEnabledConfigByCode("emb", true)).thenReturn(AiModelConfigBO.builder()
                .id(7L)
                .code("emb")
                .providerType("OPENAI_COMPATIBLE")
                .baseUrl("https://example.com/v1")
                .apiKey("sk-test")
                .modelName("text-embedding")
                .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                .timeoutSeconds(10L)
                .build());
        when(aiKnowledgeDocumentSourceService.loadSource(document)).thenReturn(AiKnowledgeDocumentSource.builder()
                .documentId(1L)
                .knowledgeBaseId(11L)
                .documentName("制度.md")
                .fileBytes("制度内容".getBytes())
                .build());
        when(aiKnowledgeDocumentParseStrategyService.parseAndChunk(any(), any())).thenReturn(List.of(
                AiKnowledgeDocumentChunkDraft.builder()
                        .knowledgeBaseId(11L)
                        .documentId(1L)
                        .chunkNo(1)
                        .content("制度内容")
                        .contentPreview("制度内容")
                        .sourcePosition("paragraph:1")
                        .tokenCount(4)
                        .build()
        ));
        when(aiKnowledgeDocumentChunkMapper.insert(any(AiKnowledgeDocumentChunkEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeDocumentChunkEntity chunk = invocation.getArgument(0);
            chunk.setId(101L);
            return 1;
        });
        when(aiKnowledgeDocumentChunkMapper.selectList(any())).thenReturn(List.of(chunk()));
        when(embeddingProviderService.embed(any())).thenReturn(AiKnowledgeEmbeddingResponse.builder()
                .knowledgeBaseId(11L)
                .documentId(1L)
                .chunkId(101L)
                .dimensions(2)
                .vector(List.of(0.1D, 0.2D))
                .vectorHash("hash")
                .providerType("OPENAI_COMPATIBLE")
                .modelName("text-embedding")
                .build());
        when(aiKnowledgeDocumentMapper.selectCount(any())).thenReturn(1L);
        when(aiKnowledgeDocumentMapper.selectList(any())).thenReturn(List.of(
                new AiKnowledgeDocumentEntity().setChunkCount(1)
        ));
    }

    private AiKnowledgeDocumentChunkEntity chunk() {
        AiKnowledgeDocumentChunkEntity chunk = new AiKnowledgeDocumentChunkEntity()
                .setKnowledgeBaseId(11L)
                .setDocumentId(1L)
                .setChunkNo(1)
                .setContent("制度内容")
                .setContentPreview("制度内容")
                .setSourcePosition("paragraph:1")
                .setTokenCount(4);
        chunk.setId(101L);
        return chunk;
    }
}
