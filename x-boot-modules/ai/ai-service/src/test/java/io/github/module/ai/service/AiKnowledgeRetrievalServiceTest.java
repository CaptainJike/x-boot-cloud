package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.entity.AiKnowledgeBaseEntity;
import io.github.module.ai.entity.AiKnowledgeRetrievalLogEntity;
import io.github.module.ai.enums.AiErrorEnum;
import io.github.module.ai.mapper.AiKnowledgeBaseMapper;
import io.github.module.ai.mapper.AiKnowledgeRetrievalLogMapper;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.service.model.AiKnowledgeVectorDocument;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchHit;
import io.github.module.ai.service.model.AiKnowledgeVectorSearchRequest;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AiKnowledgeRetrievalServiceTest {

    @Mock
    private AiKnowledgeBaseMapper aiKnowledgeBaseMapper;

    @Mock
    private AiKnowledgeRetrievalLogMapper aiKnowledgeRetrievalLogMapper;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @Mock
    private AiKnowledgeVectorRetrievalService aiKnowledgeVectorRetrievalService;

    private AiKnowledgeRetrievalService aiKnowledgeRetrievalService;

    @BeforeAll
    static void initTableInfo() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, AiKnowledgeBaseEntity.class);
        TableInfoHelper.initTableInfo(assistant, AiKnowledgeRetrievalLogEntity.class);
    }

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(9L)
                .setUserName("admin"));
        aiKnowledgeRetrievalService = new AiKnowledgeRetrievalService(
                aiKnowledgeBaseMapper,
                aiKnowledgeRetrievalLogMapper,
                aiModelConfigService,
                aiKnowledgeVectorRetrievalService
        );
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void adminRetrieveSearchesVectorStoreAndWritesSuccessLog() {
        when(aiKnowledgeBaseMapper.selectBatchIds(any())).thenReturn(List.of(enabledKnowledgeBase()));
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(modelConfig());
        when(aiKnowledgeVectorRetrievalService.search(any())).thenReturn(List.of(vectorHit()));
        when(aiKnowledgeRetrievalLogMapper.insert(any(AiKnowledgeRetrievalLogEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeRetrievalLogEntity entity = invocation.getArgument(0);
            entity.setId(100L);
            return 1;
        });

        AiKnowledgeRetrievalResultBO result = aiKnowledgeRetrievalService.adminRetrieve(
                AdminRetrieveAiKnowledgeDTO.builder()
                        .knowledgeBaseIds(List.of(1L))
                        .query(" 休假制度 ")
                        .conversationId(" conv-1 ")
                        .messageId(" msg-1 ")
                        .build()
        );

        assertThat(result.getLogId()).isEqualTo(100L);
        assertThat(result.getQuery()).isEqualTo("休假制度");
        assertThat(result.getTopK()).isEqualTo(3);
        assertThat(result.getSimilarityThreshold()).isEqualTo(0.2D);
        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getHitCount()).isEqualTo(1);
        assertThat(result.getHits().getFirst().getKnowledgeBaseName()).isEqualTo("企业知识库");
        assertThat(result.getHits().getFirst().getDocumentName()).isEqualTo("员工手册.md");

        ArgumentCaptor<AiKnowledgeVectorSearchRequest> searchCaptor =
                ArgumentCaptor.forClass(AiKnowledgeVectorSearchRequest.class);
        verify(aiKnowledgeVectorRetrievalService).search(searchCaptor.capture());
        AiKnowledgeVectorSearchRequest searchRequest = searchCaptor.getValue();
        assertThat(searchRequest.getKnowledgeBaseIds()).containsExactly(1L);
        assertThat(searchRequest.getQuery()).isEqualTo("休假制度");
        assertThat(searchRequest.getTopK()).isEqualTo(3);
        assertThat(searchRequest.getSimilarityThreshold()).isEqualTo(0.2D);
        assertThat(searchRequest.getContext().getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(searchRequest.getContext().getApiKey()).isEqualTo("sk-test");

        ArgumentCaptor<AiKnowledgeRetrievalLogEntity> logCaptor =
                ArgumentCaptor.forClass(AiKnowledgeRetrievalLogEntity.class);
        verify(aiKnowledgeRetrievalLogMapper).insert(logCaptor.capture());
        AiKnowledgeRetrievalLogEntity log = logCaptor.getValue();
        assertThat(log.getUserId()).isEqualTo(9L);
        assertThat(log.getKnowledgeBaseIds()).isEqualTo(",1,");
        assertThat(log.getConversationId()).isEqualTo("conv-1");
        assertThat(log.getMessageId()).isEqualTo("msg-1");
        assertThat(log.getQueryText()).isEqualTo("休假制度");
        assertThat(log.getStatus()).isEqualTo(1);
        assertThat(log.getHitCount()).isEqualTo(1);
        assertThat(log.getHitsSummary()).contains("员工手册.md#4@0.93");

        ArgumentCaptor<AiKnowledgeBaseEntity> knowledgeBaseCaptor =
                ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper).updateById(knowledgeBaseCaptor.capture());
        assertThat(knowledgeBaseCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(knowledgeBaseCaptor.getValue().getLastRetrievedAt()).isNotNull();
    }

    @Test
    void adminRetrieveReturnsFailureResultAndWritesLogWhenVectorSearchFails() {
        when(aiKnowledgeBaseMapper.selectBatchIds(any())).thenReturn(List.of(enabledKnowledgeBase()));
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(modelConfig());
        when(aiKnowledgeVectorRetrievalService.search(any()))
                .thenThrow(new BusinessException(AiErrorEnum.KNOWLEDGE_VECTOR_STORE_UNAVAILABLE));
        when(aiKnowledgeRetrievalLogMapper.insert(any(AiKnowledgeRetrievalLogEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeRetrievalLogEntity entity = invocation.getArgument(0);
            entity.setId(101L);
            return 1;
        });

        AiKnowledgeRetrievalResultBO result = aiKnowledgeRetrievalService.adminRetrieve(
                AdminRetrieveAiKnowledgeDTO.builder()
                        .knowledgeBaseIds(List.of(1L))
                        .query("休假制度")
                        .topK(8)
                        .similarityThreshold(0.6D)
                        .build()
        );

        assertThat(result.getLogId()).isEqualTo(101L);
        assertThat(result.getStatus()).isZero();
        assertThat(result.getHitCount()).isZero();
        assertThat(result.getHits()).isEmpty();
        assertThat(result.getErrorMessage()).isEqualTo("知识库向量存储不可用");

        ArgumentCaptor<AiKnowledgeRetrievalLogEntity> logCaptor =
                ArgumentCaptor.forClass(AiKnowledgeRetrievalLogEntity.class);
        verify(aiKnowledgeRetrievalLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getStatus()).isZero();
        assertThat(logCaptor.getValue().getErrorCode())
                .isEqualTo("KNOWLEDGE_VECTOR_STORE_UNAVAILABLE");
        verify(aiKnowledgeBaseMapper, never()).updateById(any(AiKnowledgeBaseEntity.class));
    }

    @Test
    void adminRetrieveSkipsLogWhenLogFlagFalse() {
        when(aiKnowledgeBaseMapper.selectBatchIds(any())).thenReturn(List.of(enabledKnowledgeBase()));
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(modelConfig());
        when(aiKnowledgeVectorRetrievalService.search(any())).thenReturn(List.of());

        AiKnowledgeRetrievalResultBO result = aiKnowledgeRetrievalService.adminRetrieve(
                AdminRetrieveAiKnowledgeDTO.builder()
                        .knowledgeBaseIds(List.of(1L))
                        .query("休假制度")
                        .logFlag(false)
                        .build()
        );

        assertThat(result.getStatus()).isEqualTo(1);
        assertThat(result.getLogId()).isNull();
        verify(aiKnowledgeRetrievalLogMapper, never()).insert(any(AiKnowledgeRetrievalLogEntity.class));
    }

    @Test
    void adminRetrieveCleansIdsFiltersHitsAndRefreshesAllKnowledgeBases() {
        when(aiKnowledgeBaseMapper.selectBatchIds(any())).thenReturn(List.of(
                enabledKnowledgeBase(1L, "企业知识库"),
                enabledKnowledgeBase(2L, "制度知识库")
        ));
        when(aiModelConfigService.getEnabledConfigByCode("embedding-default", true))
                .thenReturn(modelConfig());
        when(aiKnowledgeVectorRetrievalService.search(any())).thenReturn(List.of(
                vectorHit(1L, "员工手册.md", 4, 0.93D),
                vectorHit(2L, "低分文档.md", 1, 0.1D),
                vectorHit(2L, "制度问答.md", 2, null)
        ));
        when(aiKnowledgeRetrievalLogMapper.insert(any(AiKnowledgeRetrievalLogEntity.class))).thenAnswer(invocation -> {
            AiKnowledgeRetrievalLogEntity entity = invocation.getArgument(0);
            entity.setId(102L);
            return 1;
        });

        AiKnowledgeRetrievalResultBO result = aiKnowledgeRetrievalService.adminRetrieve(
                AdminRetrieveAiKnowledgeDTO.builder()
                        .knowledgeBaseIds(Arrays.asList(1L, null, 2L, 1L))
                        .query("休假制度")
                        .topK(2)
                        .similarityThreshold(0.5D)
                        .conversationId("conv-2")
                        .messageId("msg-2")
                        .build()
        );

        assertThat(result.getLogId()).isEqualTo(102L);
        assertThat(result.getHitCount()).isEqualTo(2);
        assertThat(result.getHits())
                .extracting(hit -> hit.getDocumentName())
                .containsExactly("员工手册.md", "制度问答.md");
        assertThat(result.getHits().get(1).getKnowledgeBaseName()).isEqualTo("制度知识库");

        ArgumentCaptor<AiKnowledgeVectorSearchRequest> searchCaptor =
                ArgumentCaptor.forClass(AiKnowledgeVectorSearchRequest.class);
        verify(aiKnowledgeVectorRetrievalService).search(searchCaptor.capture());
        assertThat(searchCaptor.getValue().getKnowledgeBaseIds()).containsExactly(1L, 2L);
        assertThat(searchCaptor.getValue().getTopK()).isEqualTo(2);
        assertThat(searchCaptor.getValue().getSimilarityThreshold()).isEqualTo(0.5D);

        ArgumentCaptor<AiKnowledgeRetrievalLogEntity> logCaptor =
                ArgumentCaptor.forClass(AiKnowledgeRetrievalLogEntity.class);
        verify(aiKnowledgeRetrievalLogMapper).insert(logCaptor.capture());
        assertThat(logCaptor.getValue().getKnowledgeBaseIds()).isEqualTo(",1,2,");
        assertThat(logCaptor.getValue().getConversationId()).isEqualTo("conv-2");
        assertThat(logCaptor.getValue().getMessageId()).isEqualTo("msg-2");
        assertThat(logCaptor.getValue().getHitsSummary())
                .isEqualTo("员工手册.md#4@0.93;制度问答.md#2");

        ArgumentCaptor<AiKnowledgeBaseEntity> knowledgeBaseCaptor =
                ArgumentCaptor.forClass(AiKnowledgeBaseEntity.class);
        verify(aiKnowledgeBaseMapper, times(2)).updateById(knowledgeBaseCaptor.capture());
        assertThat(knowledgeBaseCaptor.getAllValues())
                .extracting(AiKnowledgeBaseEntity::getId)
                .containsExactly(1L, 2L);
        assertThat(knowledgeBaseCaptor.getAllValues())
                .allSatisfy(entity -> assertThat(entity.getLastRetrievedAt()).isNotNull());
    }

    @Test
    void adminRetrieveRejectsDisabledKnowledgeBase() {
        AiKnowledgeBaseEntity knowledgeBase = enabledKnowledgeBase()
                .setStatus(EnabledStatusEnum.DISABLED.getValue());
        when(aiKnowledgeBaseMapper.selectBatchIds(any())).thenReturn(List.of(knowledgeBase));

        assertThatThrownBy(() -> aiKnowledgeRetrievalService.adminRetrieve(
                AdminRetrieveAiKnowledgeDTO.builder()
                        .knowledgeBaseIds(List.of(1L))
                        .query("休假制度")
                        .build()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效知识库状态");
        verifyNoInteractions(aiModelConfigService, aiKnowledgeVectorRetrievalService, aiKnowledgeRetrievalLogMapper);
    }

    @Test
    void adminListLogsReturnsPagedLogBOs() {
        AiKnowledgeRetrievalLogEntity entity = retrievalLog();
        Page<AiKnowledgeRetrievalLogEntity> entityPage = new Page<AiKnowledgeRetrievalLogEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiKnowledgeRetrievalLogMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AiKnowledgeRetrievalLogBO> result = aiKnowledgeRetrievalService.adminListLogs(
                new PageParam(),
                AdminListAiKnowledgeRetrievalLogDTO.builder()
                        .knowledgeBaseId(1L)
                        .queryKeyword("休假")
                        .status(1)
                        .build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        AiKnowledgeRetrievalLogBO bo = result.getRecords().getFirst();
        assertThat(bo.getId()).isEqualTo(100L);
        assertThat(bo.getKnowledgeBaseIds()).containsExactly(1L, 2L);
        assertThat(bo.getQuery()).isEqualTo("休假制度");
        assertThat(bo.getElapsedMillis()).isEqualTo(123L);
    }

    @Test
    void getLogByIdReturnsLogDetail() {
        when(aiKnowledgeRetrievalLogMapper.selectById(100L)).thenReturn(retrievalLog());

        AiKnowledgeRetrievalLogBO result = aiKnowledgeRetrievalService.getLogById(100L, true);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getKnowledgeBaseIds()).containsExactly(1L, 2L);
        assertThat(result.getHitsSummary()).isEqualTo("员工手册.md#4@0.93");
    }

    @Test
    void getLogByIdThrowsWhenMissingAndThrowIfInvalidId() {
        when(aiKnowledgeRetrievalLogMapper.selectById(100L)).thenReturn(null);

        assertThatThrownBy(() -> aiKnowledgeRetrievalService.getLogById(100L, true))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效ID");
    }

    private AiKnowledgeBaseEntity enabledKnowledgeBase() {
        AiKnowledgeBaseEntity entity = new AiKnowledgeBaseEntity()
                .setName("企业知识库")
                .setEmbeddingModelConfigId(9L)
                .setEmbeddingModelConfigCode("embedding-default")
                .setEmbeddingProviderType("OPENAI_COMPATIBLE")
                .setEmbeddingModelName("text-embedding-v1")
                .setRetrievalTopK(3)
                .setSimilarityThreshold(0.2D)
                .setStatus(EnabledStatusEnum.ENABLED.getValue());
        entity.setId(1L);
        return entity;
    }

    private AiKnowledgeBaseEntity enabledKnowledgeBase(Long id, String name) {
        AiKnowledgeBaseEntity entity = enabledKnowledgeBase()
                .setName(name);
        entity.setId(id);
        return entity;
    }

    private AiModelConfigBO modelConfig() {
        return AiModelConfigBO.builder()
                .id(9L)
                .code("embedding-default")
                .providerType("OPENAI_COMPATIBLE")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .apiKey("sk-test")
                .modelName("text-embedding-v1")
                .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                .timeoutSeconds(30L)
                .status(EnabledStatusEnum.ENABLED.getValue())
                .build();
    }

    private AiKnowledgeVectorSearchHit vectorHit() {
        return AiKnowledgeVectorSearchHit.builder()
                .document(AiKnowledgeVectorDocument.builder()
                        .knowledgeBaseId(1L)
                        .documentId(2L)
                        .documentName("员工手册.md")
                        .chunkId(3L)
                        .chunkNo(4)
                        .content("年假与调休制度内容")
                        .sourcePage(5)
                        .sourcePosition("p5")
                        .build())
                .similarityScore(0.93D)
                .build();
    }

    private AiKnowledgeVectorSearchHit vectorHit(Long knowledgeBaseId,
                                                 String documentName,
                                                 Integer chunkNo,
                                                 Double similarityScore) {
        return AiKnowledgeVectorSearchHit.builder()
                .document(AiKnowledgeVectorDocument.builder()
                        .knowledgeBaseId(knowledgeBaseId)
                        .documentId(20L + knowledgeBaseId)
                        .documentName(documentName)
                        .chunkId(30L + knowledgeBaseId)
                        .chunkNo(chunkNo)
                        .content("命中内容")
                        .sourcePosition("paragraph:" + chunkNo)
                        .build())
                .similarityScore(similarityScore)
                .build();
    }

    private AiKnowledgeRetrievalLogEntity retrievalLog() {
        AiKnowledgeRetrievalLogEntity entity = new AiKnowledgeRetrievalLogEntity()
                .setRetrievalId("retrieval-1")
                .setUserId(9L)
                .setKnowledgeBaseIds(",1,2,")
                .setConversationId("conv-1")
                .setMessageId("msg-1")
                .setQueryText("休假制度")
                .setTopK(3)
                .setSimilarityThreshold(0.2D)
                .setHitCount(1)
                .setHitsSummary("员工手册.md#4@0.93")
                .setElapsedMs(123L)
                .setStatus(1)
                .setRetrievedAt(LocalDateTime.of(2026, 6, 21, 16, 0));
        entity.setId(100L);
        entity.setCreatedAt(LocalDateTime.of(2026, 6, 21, 16, 0));
        return entity;
    }
}
