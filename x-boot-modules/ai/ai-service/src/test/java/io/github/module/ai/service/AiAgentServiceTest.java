package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.entity.AiAgentEntity;
import io.github.module.ai.mapper.AiAgentMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;
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
class AiAgentServiceTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                AiAgentEntity.class
        );
    }

    @Mock
    private AiAgentMapper aiAgentMapper;

    @Mock
    private AiModelConfigService aiModelConfigService;

    @InjectMocks
    private AiAgentService aiAgentService;

    @Test
    void adminListReturnsPagedAgents() {
        AiAgentEntity entity = enabledAgent();
        entity.setId(1L);
        Page<AiAgentEntity> entityPage = new Page<AiAgentEntity>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(entity));
        when(aiAgentMapper.selectPage(any(), any())).thenReturn(entityPage);

        PageResult<AiAgentBO> result = aiAgentService.adminList(
                new PageParam(),
                AdminListAiAgentDTO.builder().name("客服").build()
        );

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().getFirst().getAgentCode()).isEqualTo("customer-service");
        assertThat(result.getRecords().getFirst().getExecutionCount()).isEqualTo(3);
    }

    @Test
    void adminInsertRejectsDuplicateAgentCode() {
        AiAgentEntity existing = enabledAgent();
        existing.setId(2L);
        when(aiAgentMapper.selectOne(any())).thenReturn(existing);

        assertThatThrownBy(() -> aiAgentService.adminInsert(validDto()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("已存在相同Agent，请重新输入");
        verify(aiAgentMapper, never()).insert(any(AiAgentEntity.class));
    }

    @Test
    void adminInsertNormalizesDefaultsAndSnapshotsModelConfig() {
        when(aiAgentMapper.selectOne(any())).thenReturn(null);
        when(aiModelConfigService.getEnabledConfigByCode("chat-default", true))
                .thenReturn(AiModelConfigBO.builder()
                        .id(9L)
                        .code("chat-default")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("gpt-4.1-mini")
                        .build());
        when(aiAgentMapper.insert(any(AiAgentEntity.class))).thenAnswer(invocation -> {
            AiAgentEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        Long id = aiAgentService.adminInsert(validDto()
                .setAgentCode(" customer-service ")
                .setName(" 客服 Agent ")
                .setDescription(" ")
                .setSystemPrompt("  你是客服助手  ")
                .setModelConfigCode(" chat-default "));

        assertThat(id).isEqualTo(1L);
        ArgumentCaptor<AiAgentEntity> entityCaptor = ArgumentCaptor.forClass(AiAgentEntity.class);
        verify(aiAgentMapper).insert(entityCaptor.capture());
        AiAgentEntity entity = entityCaptor.getValue();
        assertThat(entity.getAgentCode()).isEqualTo("customer-service");
        assertThat(entity.getName()).isEqualTo("客服Agent");
        assertThat(entity.getDescription()).isEmpty();
        assertThat(entity.getSystemPrompt()).isEqualTo("你是客服助手");
        assertThat(entity.getModelConfigId()).isEqualTo(9L);
        assertThat(entity.getModelConfigCode()).isEqualTo("chat-default");
        assertThat(entity.getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(entity.getModelName()).isEqualTo("gpt-4.1-mini");
        assertThat(entity.getPublishStatus()).isZero();
        assertThat(entity.getExecutionCount()).isZero();
    }

    @Test
    void getOneByIdReturnsDetail() {
        AiAgentEntity entity = enabledAgent();
        entity.setId(1L);
        entity.setPublishedAt(LocalDateTime.of(2026, 6, 22, 9, 0));
        when(aiAgentMapper.selectById(1L)).thenReturn(entity);

        AiAgentDetailBO result = aiAgentService.getOneById(1L, true);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getAgentCode()).isEqualTo("customer-service");
        assertThat(result.getPublishedAt()).isEqualTo(LocalDateTime.of(2026, 6, 22, 9, 0));
    }

    @Test
    void adminUpdateStatusRejectsInvalidStatus() {
        assertThatThrownBy(() -> aiAgentService.adminUpdateStatus(1L, 9))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效Agent状态");
        verify(aiAgentMapper, never()).selectById(any());
        verify(aiAgentMapper, never()).updateById(any(AiAgentEntity.class));
    }

    @Test
    void adminUpdateStatusUpdatesExistingAgent() {
        AiAgentEntity existing = enabledAgent();
        existing.setId(1L);
        when(aiAgentMapper.selectById(1L)).thenReturn(existing);

        aiAgentService.adminUpdateStatus(1L, EnabledStatusEnum.DISABLED.getValue());

        ArgumentCaptor<AiAgentEntity> entityCaptor = ArgumentCaptor.forClass(AiAgentEntity.class);
        verify(aiAgentMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(entityCaptor.getValue().getStatus()).isEqualTo(EnabledStatusEnum.DISABLED.getValue());
    }

    @Test
    void adminSelectOptionsReturnsEnabledAgents() {
        AiAgentEntity entity = enabledAgent();
        entity.setId(1L);
        when(aiAgentMapper.selectList(any())).thenReturn(List.of(entity));

        List<AiAgentBO> result = aiAgentService.adminSelectOptions();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getName()).isEqualTo("客服Agent");
    }

    private AdminInsertOrUpdateAiAgentDTO validDto() {
        return AdminInsertOrUpdateAiAgentDTO.builder()
                .agentCode("customer-service")
                .name("客服Agent")
                .description("企业客服")
                .avatar("https://example.com/avatar.png")
                .systemPrompt("你是客服助手")
                .modelConfigCode("chat-default")
                .knowledgeBaseIds("1,2")
                .temperature(0.7D)
                .maxTokens(2048)
                .executionConfig("{}")
                .status(EnabledStatusEnum.ENABLED.getValue())
                .build();
    }

    private AiAgentEntity enabledAgent() {
        return new AiAgentEntity()
                .setAgentCode("customer-service")
                .setName("客服Agent")
                .setDescription("企业客服")
                .setAvatar("https://example.com/avatar.png")
                .setSystemPrompt("你是客服助手")
                .setModelConfigId(9L)
                .setModelConfigCode("chat-default")
                .setProviderType("OPENAI_COMPATIBLE")
                .setModelName("gpt-4.1-mini")
                .setKnowledgeBaseIds("1,2")
                .setTemperature(0.7D)
                .setMaxTokens(2048)
                .setExecutionConfig("{}")
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setPublishStatus(0)
                .setExecutionCount(3);
    }
}
