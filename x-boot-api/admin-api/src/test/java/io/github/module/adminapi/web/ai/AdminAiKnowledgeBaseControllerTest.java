package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
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
class AdminAiKnowledgeBaseControllerTest {

    @Mock
    private AiKnowledgeBaseFacade aiKnowledgeBaseFacade;

    private AdminAiKnowledgeBaseController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiKnowledgeBaseController();
        ReflectionTestUtils.setField(controller, "aiKnowledgeBaseFacade", aiKnowledgeBaseFacade);
    }

    @Test
    void listReturnsPagedKnowledgeBases() {
        PageResult<AiKnowledgeBaseBO> pageResult = new PageResult<AiKnowledgeBaseBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiKnowledgeBaseBO.builder()
                        .id(1L)
                        .name("企业知识库")
                        .status(EnabledStatusEnum.ENABLED.getValue())
                        .build()));
        when(aiKnowledgeBaseFacade.adminList(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiKnowledgeBaseBO>> result = controller.list(
                new PageParam(),
                new AdminListAiKnowledgeBaseDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getName()).isEqualTo("企业知识库");
    }

    @Test
    void optionsReturnsEnabledKnowledgeBases() {
        when(aiKnowledgeBaseFacade.adminSelectOptions())
                .thenReturn(List.of(AiKnowledgeBaseBO.builder().id(1L).name("企业知识库").build()));

        ApiResult<List<AiKnowledgeBaseBO>> result = controller.options();

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData()).hasSize(1);
        assertThat(result.getData().getFirst().getName()).isEqualTo("企业知识库");
        verify(aiKnowledgeBaseFacade).adminSelectOptions();
    }

    @Test
    void getByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiKnowledgeBaseFacade.getOneById(7L, true))
                .thenReturn(AiKnowledgeBaseDetailBO.builder().id(7L).name("企业知识库").build());

        ApiResult<AiKnowledgeBaseDetailBO> result = controller.getById(7L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(7L);
        verify(aiKnowledgeBaseFacade).getOneById(7L, true);
    }

    @Test
    void updateSetsPathIdIntoDto() {
        AdminInsertOrUpdateAiKnowledgeBaseDTO dto = new AdminInsertOrUpdateAiKnowledgeBaseDTO();

        controller.update(9L, dto);

        assertThat(dto.getId()).isEqualTo(9L);
        verify(aiKnowledgeBaseFacade).adminUpdate(dto);
    }

    @Test
    void deletePassesIdsToFacade() {
        controller.delete(new IdsDTO<Long>().setIds(List.of(1L, 2L)));

        verify(aiKnowledgeBaseFacade).adminDelete(List.of(1L, 2L));
    }

    @Test
    void updateStatusPassesPathVariablesToFacade() {
        ApiResult<Void> result = controller.updateStatus(1L, EnabledStatusEnum.DISABLED.getValue());

        assertThat(result.getCode()).isEqualTo(200);
        verify(aiKnowledgeBaseFacade).adminUpdateStatus(1L, EnabledStatusEnum.DISABLED.getValue());
    }

    @Test
    void endpointsUseExpectedPermissions() throws Exception {
        assertPermission("list", "AiKnowledge:retrieve", PageParam.class, AdminListAiKnowledgeBaseDTO.class);
        assertPermission("options", "AiKnowledge:retrieve");
        assertPermission("getById", "AiKnowledge:retrieve", Long.class);
        assertPermission("insert", "AiKnowledge:create", AdminInsertOrUpdateAiKnowledgeBaseDTO.class);
        assertPermission("update", "AiKnowledge:update", Long.class, AdminInsertOrUpdateAiKnowledgeBaseDTO.class);
        assertPermission("delete", "AiKnowledge:delete", IdsDTO.class);
        assertPermission("updateStatus", "AiKnowledge:enable", Long.class, Integer.class);
    }

    private void assertPermission(String methodName, String value, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = AdminAiKnowledgeBaseController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        assertThat(permission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(permission.value()).containsExactly(value);
        assertThat(permission.orRole()).containsExactly("SuperAdmin");
    }
}
