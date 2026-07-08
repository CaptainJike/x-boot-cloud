package io.github.module.adminapi.web.common;

import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.model.response.AdminSelectOptionItemVO;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.facade.AiAgentFacade;
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.facade.AiWorkflowFacade;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.sys.facade.SysDataDictFacade;
import io.github.module.sys.facade.SysDeptFacade;
import io.github.module.sys.facade.SysMenuFacade;
import io.github.module.sys.facade.SysRoleFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSelectOptionsControllerTest {

    @Mock
    private SysRoleFacade sysRoleFacade;

    @Mock
    private SysDeptFacade sysDeptFacade;

    @Mock
    private SysMenuFacade sysMenuFacade;

    @Mock
    private SysDataDictFacade sysDataDictFacade;

    @Mock
    private AiModelConfigFacade aiModelConfigFacade;

    @Mock
    private AiKnowledgeBaseFacade aiKnowledgeBaseFacade;

    @Mock
    private AiAgentFacade aiAgentFacade;

    @Mock
    private AiWorkflowFacade aiWorkflowFacade;

    private AdminSelectOptionsController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSelectOptionsController();
        ReflectionTestUtils.setField(controller, "sysRoleFacade", sysRoleFacade);
        ReflectionTestUtils.setField(controller, "sysDeptFacade", sysDeptFacade);
        ReflectionTestUtils.setField(controller, "sysMenuFacade", sysMenuFacade);
        ReflectionTestUtils.setField(controller, "sysDataDictFacade", sysDataDictFacade);
        ReflectionTestUtils.setField(controller, "aiModelConfigFacade", aiModelConfigFacade);
        ReflectionTestUtils.setField(controller, "aiKnowledgeBaseFacade", aiKnowledgeBaseFacade);
        ReflectionTestUtils.setField(controller, "aiAgentFacade", aiAgentFacade);
        ReflectionTestUtils.setField(controller, "aiWorkflowFacade", aiWorkflowFacade);
    }

    @Test
    void aiModelConfigsUseConfigCodeAsSelectValue() {
        when(aiModelConfigFacade.adminSelectOptions()).thenReturn(List.of(AiModelConfigBO.builder()
                .id(1L)
                .code("embedding-default")
                .name("通义向量")
                .providerType("OPENAI_COMPATIBLE")
                .modelName("text-embedding-v1")
                .supportedModalities("text")
                .supportedCapabilities(AiModelCapabilityConstant.CHAT)
                .description("知识库向量化")
                .build()));

        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiModelConfigs();

        assertThat(result.getCode()).isEqualTo(200);
        AdminSelectOptionItemVO option = result.getData().getFirst();
        assertThat(option.getId()).isEqualTo(1L);
        assertThat(option.getValue()).isEqualTo("embedding-default");
        assertThat(option.getLabel()).isEqualTo("通义向量（embedding-default / text-embedding-v1）");
        assertThat(option.getCode()).isEqualTo("embedding-default");
        assertThat(option.getProviderType()).isEqualTo("OPENAI_COMPATIBLE");
        assertThat(option.getModelName()).isEqualTo("text-embedding-v1");
        assertThat(option.getSupportedCapabilities()).isEqualTo(AiModelCapabilityConstant.CHAT);
    }

    @Test
    void aiModelConfigsFilterNonChatCapabilities() {
        when(aiModelConfigFacade.adminSelectOptions()).thenReturn(List.of(
                AiModelConfigBO.builder()
                        .id(1L)
                        .code("chat-model")
                        .name("聊天模型")
                        .providerType("OPENAI")
                        .modelName("gpt-4o-mini")
                        .supportedCapabilities(AiModelCapabilityConstant.CHAT)
                        .build(),
                AiModelConfigBO.builder()
                        .id(2L)
                        .code("embedding-only")
                        .name("向量模型")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("text-embedding-v1")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build()
        ));

        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiModelConfigs();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getValue()).isEqualTo("chat-model");
    }

    @Test
    void aiEmbeddingModelConfigsFilterNonEmbeddingOrUnsupportedProviders() {
        when(aiModelConfigFacade.adminSelectOptions()).thenReturn(List.of(
                AiModelConfigBO.builder()
                        .id(1L)
                        .code("qwen-embedding")
                        .name("通义向量")
                        .providerType("OPENAI_COMPATIBLE")
                        .modelName("text-embedding-v1")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build(),
                AiModelConfigBO.builder()
                        .id(2L)
                        .code("zhipu-embedding")
                        .name("智谱向量")
                        .providerType("ZHIPU")
                        .modelName("embedding-3")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build(),
                AiModelConfigBO.builder()
                        .id(3L)
                        .code("openai-chat")
                        .name("聊天模型")
                        .providerType("OPENAI")
                        .modelName("gpt-4o-mini")
                        .supportedCapabilities(AiModelCapabilityConstant.CHAT)
                        .build(),
                AiModelConfigBO.builder()
                        .id(4L)
                        .code("local-ollama")
                        .name("本地Ollama")
                        .providerType("OLLAMA")
                        .modelName("nomic-embed-text")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build()
        ));

        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiEmbeddingModelConfigs();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(2);
        assertThat(result.getData())
                .extracting(AdminSelectOptionItemVO::getValue)
                .containsExactly("qwen-embedding", "zhipu-embedding");
    }

    @Test
    void aiEmbeddingModelConfigsKeepZhiPuProviderMetadata() {
        when(aiModelConfigFacade.adminSelectOptions()).thenReturn(List.of(
                AiModelConfigBO.builder()
                        .id(2L)
                        .code("zhipu-embedding")
                        .name("智谱向量")
                        .providerType("ZHIPU")
                        .modelName("embedding-3")
                        .supportedCapabilities(AiModelCapabilityConstant.EMBEDDING)
                        .build()
        ));

        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiEmbeddingModelConfigs();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getProviderType()).isEqualTo("ZHIPU");
        assertThat(result.getData().getFirst().getModelName()).isEqualTo("embedding-3");
    }

    @Test
    void aiWorkflowsReturnEnabledWorkflowOptions() {
        when(aiWorkflowFacade.adminSelectOptions()).thenReturn(List.of(AiWorkflowBO.builder()
                .id(10L)
                .workflowCode("customer-flow")
                .name("客服流程")
                .versionNo(2)
                .build()));

        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiWorkflows();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getFirst().getValue()).isEqualTo(10L);
        assertThat(result.getData().getFirst().getLabel()).isEqualTo("客服流程（customer-flow / v2）");
        verify(aiWorkflowFacade).adminSelectOptions();
    }

    @Test
    void aiProviderTypesReturnStringValueOptions() {
        ApiResult<List<AdminSelectOptionItemVO>> result = controller.aiProviderTypes();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData())
                .extracting(AdminSelectOptionItemVO::getValue)
                .contains("OPENAI", "OPENAI_COMPATIBLE", "DEEPSEEK", "ZHIPU", "OLLAMA");
    }
}
