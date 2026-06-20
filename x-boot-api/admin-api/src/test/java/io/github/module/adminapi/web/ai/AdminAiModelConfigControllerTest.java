package io.github.module.adminapi.web.ai;

import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListAiModelConfigDTO;
import io.github.module.ai.model.request.AdminListProviderModelDTO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiModelConfigTestBO;
import io.github.module.ai.model.response.AiProviderModelBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiModelConfigControllerTest {

    @Mock
    private AiModelConfigFacade aiModelConfigFacade;

    private AdminAiModelConfigController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiModelConfigController();
        ReflectionTestUtils.setField(controller, "aiModelConfigFacade", aiModelConfigFacade);
    }

    @Test
    void listReturnsPagedModelConfigs() {
        PageResult<AiModelConfigBO> pageResult = new PageResult<AiModelConfigBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiModelConfigBO.builder().id(1L).name("默认模型").build()));
        when(aiModelConfigFacade.adminList(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiModelConfigBO>> result = controller.list(new PageParam(), new AdminListAiModelConfigDTO());

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getName()).isEqualTo("默认模型");
    }

    @Test
    void updateSetsPathIdIntoDto() {
        AdminInsertOrUpdateAiModelConfigDTO dto = new AdminInsertOrUpdateAiModelConfigDTO();

        controller.update(9L, dto);

        verify(aiModelConfigFacade).adminUpdate(eq(dto));
        assertThat(dto.getId()).isEqualTo(9L);
    }

    @Test
    void getApiKeyReturnsFullApiKey() {
        when(aiModelConfigFacade.adminGetApiKey(9L)).thenReturn("sk-full-key");

        ApiResult<String> result = controller.getApiKey(9L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).isEqualTo("sk-full-key");
        verify(aiModelConfigFacade).adminGetApiKey(9L);
    }

    @Test
    void deletePassesIdsToFacade() {
        controller.delete(new IdsDTO<Long>().setIds(List.of(1L, 2L)));

        verify(aiModelConfigFacade).adminDelete(List.of(1L, 2L));
    }

    @Test
    void testPassesIdToFacade() {
        when(aiModelConfigFacade.adminTest(1L))
                .thenReturn(AiModelConfigTestBO.builder().success(true).build());

        ApiResult<AiModelConfigTestBO> result = controller.test(1L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getSuccess()).isTrue();
        verify(aiModelConfigFacade).adminTest(1L);
    }

    @Test
    void providerModelsPassesDtoToFacade() {
        AdminListProviderModelDTO dto = AdminListProviderModelDTO.builder()
                .providerType("DEEPSEEK")
                .apiKey("sk-test")
                .build();
        when(aiModelConfigFacade.adminListProviderModels(dto))
                .thenReturn(List.of(AiProviderModelBO.builder().id("deepseek-chat").name("deepseek-chat").build()));

        ApiResult<List<AiProviderModelBO>> result = controller.providerModels(dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getFirst().getId()).isEqualTo("deepseek-chat");
        verify(aiModelConfigFacade).adminListProviderModels(dto);
    }
}
