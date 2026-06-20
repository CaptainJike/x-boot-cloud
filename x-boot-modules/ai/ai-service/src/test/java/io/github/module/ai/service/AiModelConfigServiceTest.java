package io.github.module.ai.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.entity.AiModelConfigEntity;
import io.github.module.ai.mapper.AiModelConfigMapper;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.starter.ai.service.XBootAiService;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiModelConfigServiceTest {

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                AiModelConfigEntity.class
        );
    }

    @Mock
    private AiModelConfigMapper aiModelConfigMapper;

    @Mock
    private XBootAiService xBootAiService;

    @InjectMocks
    private AiModelConfigService aiModelConfigService;

    @Test
    void adminInsertRejectsInvalidProviderType() {
        when(aiModelConfigMapper.selectOne(any())).thenReturn(null);

        AdminInsertOrUpdateAiModelConfigDTO dto = validDto().setProviderType("BAD_PROVIDER");

        assertThatThrownBy(() -> aiModelConfigService.adminInsert(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessage("无效AI模型供应商类型");
        verify(aiModelConfigMapper, never()).insert(any(AiModelConfigEntity.class));
    }

    @Test
    void adminInsertClearsOtherDefaultWhenNewConfigIsDefault() {
        when(aiModelConfigMapper.selectOne(any())).thenReturn(null);
        when(aiModelConfigMapper.insert(any(AiModelConfigEntity.class))).thenAnswer(invocation -> {
            AiModelConfigEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            return 1;
        });

        aiModelConfigService.adminInsert(validDto().setProviderType("qwen").setApiKey("sk-test"));

        verify(aiModelConfigMapper).update(
                argThat(entity -> YesOrNoEnum.NO.getValue().equals(entity.getDefaultFlag())),
                any()
        );
        ArgumentCaptor<AiModelConfigEntity> entityCaptor = ArgumentCaptor.forClass(AiModelConfigEntity.class);
        verify(aiModelConfigMapper).insert(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
    }

    @Test
    void adminSelectOptionsReturnsEnabledConfigs() {
        AiModelConfigEntity entity = new AiModelConfigEntity()
                .setCode("default")
                .setName("默认模型")
                .setProviderType("OLLAMA")
                .setModelName("llama3.2")
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setDefaultFlag(YesOrNoEnum.YES.getValue());
        entity.setId(1L);
        when(aiModelConfigMapper.selectList(any())).thenReturn(List.of(entity));

        List<AiModelConfigBO> options = aiModelConfigService.adminSelectOptions();

        assertThat(options).hasSize(1);
        assertThat(options.getFirst().getId()).isEqualTo(1L);
        assertThat(options.getFirst().getCode()).isEqualTo("default");
        assertThat(options.getFirst().getName()).isEqualTo("默认模型");
    }

    @Test
    void adminTestReadsStoredApiKey() {
        AiModelConfigEntity entity = enabledEntity()
                .setProviderType("DEEPSEEK")
                .setBaseUrl("https://api.deepseek.com")
                .setApiKey("sk-test")
                .setModelName("deepseek-chat");
        entity.setId(1L);
        when(aiModelConfigMapper.selectById(1L)).thenReturn(entity);
        when(xBootAiService.chat(any(), any())).thenReturn("OK");

        AiModelConfigTestBO result = aiModelConfigService.adminTest(1L);

        assertThat(result.getSuccess()).isTrue();
        assertThat(result.getApiKeyPresent()).isTrue();
        assertThat(result.getAnswerPreview()).isEqualTo("OK");
    }

    @Test
    void adminGetApiKeyReturnsStoredFullApiKey() {
        AiModelConfigEntity entity = enabledEntity().setApiKey("  sk-full-key  ");
        entity.setId(1L);
        when(aiModelConfigMapper.selectById(1L)).thenReturn(entity);

        String apiKey = aiModelConfigService.adminGetApiKey(1L);

        assertThat(apiKey).isEqualTo("sk-full-key");
    }

    @Test
    void adminUpdateKeepsExistingApiKeyWhenInputIsBlank() {
        AiModelConfigEntity existing = enabledEntity()
                .setProviderType("DEEPSEEK")
                .setApiKey("sk-existing")
                .setModelName("deepseek-chat");
        existing.setId(1L);
        when(aiModelConfigMapper.selectById(1L)).thenReturn(existing);
        when(aiModelConfigMapper.selectOne(any())).thenReturn(existing);

        aiModelConfigService.adminUpdate(validDto()
                .setId(1L)
                .setProviderType("DEEPSEEK")
                .setApiKey("")
                .setModelName("deepseek-chat"));

        ArgumentCaptor<AiModelConfigEntity> entityCaptor = ArgumentCaptor.forClass(AiModelConfigEntity.class);
        verify(aiModelConfigMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getApiKey()).isEqualTo("sk-existing");
    }

    private AdminInsertOrUpdateAiModelConfigDTO validDto() {
        return AdminInsertOrUpdateAiModelConfigDTO.builder()
                .code("default")
                .name("默认模型")
                .providerType("OLLAMA")
                .baseUrl("http://localhost:11434")
                .modelName("llama3.2")
                .status(EnabledStatusEnum.ENABLED.getValue())
                .defaultFlag(YesOrNoEnum.YES.getValue())
                .description("test")
                .build();
    }

    private AiModelConfigEntity enabledEntity() {
        return new AiModelConfigEntity()
                .setCode("default")
                .setName("默认模型")
                .setProviderType("OLLAMA")
                .setBaseUrl("http://localhost:11434")
                .setModelName("llama3.2")
                .setStatus(EnabledStatusEnum.ENABLED.getValue())
                .setDefaultFlag(YesOrNoEnum.YES.getValue());
    }
}
