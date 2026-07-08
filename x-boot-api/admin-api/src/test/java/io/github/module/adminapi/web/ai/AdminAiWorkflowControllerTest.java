package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiWorkflowFacade;
import io.github.module.ai.model.request.AdminExecuteAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiWorkflowNodeDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowExecutionDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowDTO;
import io.github.module.ai.model.request.AdminListAiWorkflowNodeDTO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionDetailBO;
import io.github.module.ai.model.response.AdminAiWorkflowExecutionResultBO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.ai.model.response.AiWorkflowDetailBO;
import io.github.module.ai.model.response.AiWorkflowNodeBO;
import io.github.module.ai.model.response.AiWorkflowNodeDetailBO;
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
class AdminAiWorkflowControllerTest {

    @Mock
    private AiWorkflowFacade aiWorkflowFacade;

    private AdminAiWorkflowController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiWorkflowController();
        ReflectionTestUtils.setField(controller, "aiWorkflowFacade", aiWorkflowFacade);
    }

    @Test
    void listReturnsPagedWorkflows() {
        PageResult<AiWorkflowBO> pageResult = new PageResult<AiWorkflowBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiWorkflowBO.builder()
                        .id(1L)
                        .workflowCode("customer-flow")
                        .name("客服流程")
                        .status(EnabledStatusEnum.ENABLED.getValue())
                        .build()));
        when(aiWorkflowFacade.adminList(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiWorkflowBO>> result = controller.list(
                new PageParam(),
                new AdminListAiWorkflowDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getWorkflowCode()).isEqualTo("customer-flow");
    }

    @Test
    void optionsReturnsEnabledWorkflows() {
        when(aiWorkflowFacade.adminSelectOptions())
                .thenReturn(List.of(AiWorkflowBO.builder().id(1L).name("客服流程").build()));

        ApiResult<List<AiWorkflowBO>> result = controller.options();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getName()).isEqualTo("客服流程");
        verify(aiWorkflowFacade).adminSelectOptions();
    }

    @Test
    void getByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiWorkflowFacade.getOneById(7L, true))
                .thenReturn(AiWorkflowDetailBO.builder().id(7L).workflowCode("customer-flow").build());

        ApiResult<AiWorkflowDetailBO> result = controller.getById(7L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(7L);
        verify(aiWorkflowFacade).getOneById(7L, true);
    }

    @Test
    void updateSetsPathIdIntoDto() {
        AdminInsertOrUpdateAiWorkflowDTO dto = new AdminInsertOrUpdateAiWorkflowDTO();

        controller.update(9L, dto);

        assertThat(dto.getId()).isEqualTo(9L);
        verify(aiWorkflowFacade).adminUpdate(dto);
    }

    @Test
    void deleteAndStatusPassThroughFacade() {
        controller.delete(new IdsDTO<Long>().setIds(List.of(1L, 2L)));
        ApiResult<Void> statusResult = controller.updateStatus(1L, EnabledStatusEnum.DISABLED.getValue());

        assertThat(statusResult.getCode()).isEqualTo(200);
        verify(aiWorkflowFacade).adminDelete(List.of(1L, 2L));
        verify(aiWorkflowFacade).adminUpdateStatus(1L, EnabledStatusEnum.DISABLED.getValue());
    }

    @Test
    void listNodesSetsWorkflowDefinitionIdIntoDto() {
        when(aiWorkflowFacade.adminListNodes(any())).thenReturn(List.of(AiWorkflowNodeBO.builder()
                .id(10L)
                .workflowDefinitionId(1L)
                .nodeKey("start")
                .build()));
        AdminListAiWorkflowNodeDTO dto = new AdminListAiWorkflowNodeDTO();

        ApiResult<List<AiWorkflowNodeBO>> result = controller.listNodes(1L, dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(dto.getWorkflowDefinitionId()).isEqualTo(1L);
        assertThat(result.getData().getFirst().getNodeKey()).isEqualTo("start");
        verify(aiWorkflowFacade).adminListNodes(dto);
    }

    @Test
    void getNodeByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiWorkflowFacade.getNodeById(10L, true))
                .thenReturn(AiWorkflowNodeDetailBO.builder().id(10L).nodeKey("start").build());

        ApiResult<AiWorkflowNodeDetailBO> result = controller.getNodeById(10L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(10L);
        verify(aiWorkflowFacade).getNodeById(10L, true);
    }

    @Test
    void nodeWriteMethodsSetPathVariablesIntoDto() {
        AdminInsertOrUpdateAiWorkflowNodeDTO insertDto = new AdminInsertOrUpdateAiWorkflowNodeDTO();
        AdminInsertOrUpdateAiWorkflowNodeDTO updateDto = new AdminInsertOrUpdateAiWorkflowNodeDTO();

        controller.insertNode(1L, insertDto);
        controller.updateNode(1L, 10L, updateDto);
        controller.deleteNodes(new IdsDTO<Long>().setIds(List.of(10L, 11L)));

        assertThat(insertDto.getWorkflowDefinitionId()).isEqualTo(1L);
        assertThat(updateDto.getWorkflowDefinitionId()).isEqualTo(1L);
        assertThat(updateDto.getId()).isEqualTo(10L);
        verify(aiWorkflowFacade).adminInsertNode(insertDto);
        verify(aiWorkflowFacade).adminUpdateNode(updateDto);
        verify(aiWorkflowFacade).adminDeleteNodes(List.of(10L, 11L));
    }

    @Test
    void executeSetsWorkflowDefinitionIdAndReturnsResult() {
        when(aiWorkflowFacade.adminExecute(any(), any())).thenReturn(AdminAiWorkflowExecutionResultBO.builder()
                .id(100L)
                .executionId("exec-1")
                .status(1)
                .finalOutput("done")
                .build());
        AdminExecuteAiWorkflowDTO dto = new AdminExecuteAiWorkflowDTO();

        ApiResult<AdminAiWorkflowExecutionResultBO> result = controller.execute(1L, dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(dto.getWorkflowDefinitionId()).isEqualTo(1L);
        assertThat(result.getData().getExecutionId()).isEqualTo("exec-1");
        verify(aiWorkflowFacade).adminExecute(1L, dto);
    }

    @Test
    void listExecutionsReturnsPagedExecutionRecords() {
        PageResult<AdminAiWorkflowExecutionBO> pageResult = new PageResult<AdminAiWorkflowExecutionBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AdminAiWorkflowExecutionBO.builder()
                        .id(100L)
                        .executionId("exec-1")
                        .workflowName("客服流程")
                        .status(1)
                        .build()));
        when(aiWorkflowFacade.adminListExecutions(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AdminAiWorkflowExecutionBO>> result = controller.listExecutions(
                new PageParam(),
                new AdminListAiWorkflowExecutionDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getExecutionId()).isEqualTo("exec-1");
    }

    @Test
    void getExecutionByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiWorkflowFacade.getExecutionById(100L, true))
                .thenReturn(AdminAiWorkflowExecutionDetailBO.builder().id(100L).executionId("exec-1").build());

        ApiResult<AdminAiWorkflowExecutionDetailBO> result = controller.getExecutionById(100L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(100L);
        verify(aiWorkflowFacade).getExecutionById(100L, true);
    }

    @Test
    void endpointsUseExpectedPermissions() throws Exception {
        assertPermission("list", "AiWorkflow:retrieve", PageParam.class, AdminListAiWorkflowDTO.class);
        assertPermission("options", "AiWorkflow:retrieve");
        assertPermission("getById", "AiWorkflow:retrieve", Long.class);
        assertPermission("insert", "AiWorkflow:create", AdminInsertOrUpdateAiWorkflowDTO.class);
        assertPermission("update", "AiWorkflow:update", Long.class, AdminInsertOrUpdateAiWorkflowDTO.class);
        assertPermission("delete", "AiWorkflow:delete", IdsDTO.class);
        assertPermission("updateStatus", "AiWorkflow:enable", Long.class, Integer.class);
        assertPermission("listNodes", "AiWorkflow:retrieve", Long.class, AdminListAiWorkflowNodeDTO.class);
        assertPermission("getNodeById", "AiWorkflow:retrieve", Long.class);
        assertPermission("insertNode", "AiWorkflow:create", Long.class, AdminInsertOrUpdateAiWorkflowNodeDTO.class);
        assertPermission(
                "updateNode",
                "AiWorkflow:update",
                Long.class,
                Long.class,
                AdminInsertOrUpdateAiWorkflowNodeDTO.class
        );
        assertPermission("deleteNodes", "AiWorkflow:delete", IdsDTO.class);
        assertPermission("execute", "AiWorkflow:execute", Long.class, AdminExecuteAiWorkflowDTO.class);
        assertPermission(
                "listExecutions",
                "AiWorkflow:retrieve",
                PageParam.class,
                AdminListAiWorkflowExecutionDTO.class
        );
        assertPermission("getExecutionById", "AiWorkflow:retrieve", Long.class);
    }

    private void assertPermission(String methodName, String value, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = AdminAiWorkflowController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        assertThat(permission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(permission.value()).containsExactly(value);
        assertThat(permission.orRole()).containsExactly("SuperAdmin");
    }
}
