package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiAgentFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiAgentDTO;
import io.github.module.ai.model.request.AdminListAiAgentDTO;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiAgentDetailBO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAiAgentControllerTest {

    @Mock
    private AiAgentFacade aiAgentFacade;

    private AdminAiAgentController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiAgentController();
        ReflectionTestUtils.setField(controller, "aiAgentFacade", aiAgentFacade);
    }

    @Test
    void listReturnsPagedAgents() {
        PageResult<AiAgentBO> pageResult = new PageResult<AiAgentBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiAgentBO.builder()
                        .id(1L)
                        .agentCode("customer-service")
                        .name("客服Agent")
                        .status(EnabledStatusEnum.ENABLED.getValue())
                        .build()));
        when(aiAgentFacade.adminList(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiAgentBO>> result = controller.list(new PageParam(), new AdminListAiAgentDTO());

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getAgentCode()).isEqualTo("customer-service");
    }

    @Test
    void optionsReturnsEnabledAgents() {
        when(aiAgentFacade.adminSelectOptions())
                .thenReturn(List.of(AiAgentBO.builder().id(1L).name("客服Agent").build()));

        ApiResult<List<AiAgentBO>> result = controller.options();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getName()).isEqualTo("客服Agent");
        verify(aiAgentFacade).adminSelectOptions();
    }

    @Test
    void getByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiAgentFacade.getOneById(7L, true))
                .thenReturn(AiAgentDetailBO.builder().id(7L).agentCode("customer-service").build());

        ApiResult<AiAgentDetailBO> result = controller.getById(7L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(7L);
        verify(aiAgentFacade).getOneById(7L, true);
    }

    @Test
    void updateSetsPathIdIntoDto() {
        AdminInsertOrUpdateAiAgentDTO dto = new AdminInsertOrUpdateAiAgentDTO();

        controller.update(9L, dto);

        assertThat(dto.getId()).isEqualTo(9L);
        verify(aiAgentFacade).adminUpdate(dto);
    }

    @Test
    void deletePassesIdsToFacade() {
        controller.delete(new IdsDTO<Long>().setIds(List.of(1L, 2L)));

        verify(aiAgentFacade).adminDelete(List.of(1L, 2L));
    }

    @Test
    void updateStatusPassesPathVariablesToFacade() {
        ApiResult<Void> result = controller.updateStatus(1L, EnabledStatusEnum.DISABLED.getValue());

        assertThat(result.getCode()).isEqualTo(200);
        verify(aiAgentFacade).adminUpdateStatus(1L, EnabledStatusEnum.DISABLED.getValue());
    }

    @Test
    void endpointsUseExpectedPermissions() throws Exception {
        assertPermission("list", "AiAgent:retrieve", PageParam.class, AdminListAiAgentDTO.class);
        assertPermission("options", "AiAgent:retrieve");
        assertPermission("getById", "AiAgent:retrieve", Long.class);
        assertPermission("insert", "AiAgent:create", AdminInsertOrUpdateAiAgentDTO.class);
        assertPermission("update", "AiAgent:update", Long.class, AdminInsertOrUpdateAiAgentDTO.class);
        assertPermission("delete", "AiAgent:delete", IdsDTO.class);
        assertPermission("updateStatus", "AiAgent:enable", Long.class, Integer.class);
    }

    private void assertPermission(String methodName, String value, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = AdminAiAgentController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        assertThat(permission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(permission.value()).containsExactly(value);
        assertThat(permission.orRole()).containsExactly("SuperAdmin");
    }
}
