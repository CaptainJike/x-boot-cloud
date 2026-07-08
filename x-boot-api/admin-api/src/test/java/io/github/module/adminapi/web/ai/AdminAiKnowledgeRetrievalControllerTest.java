package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiKnowledgeRetrievalFacade;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
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
class AdminAiKnowledgeRetrievalControllerTest {

    @Mock
    private AiKnowledgeRetrievalFacade aiKnowledgeRetrievalFacade;

    private AdminAiKnowledgeRetrievalController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiKnowledgeRetrievalController();
        ReflectionTestUtils.setField(controller, "aiKnowledgeRetrievalFacade", aiKnowledgeRetrievalFacade);
    }

    @Test
    void retrieveCallsFacadeAndReturnsHits() {
        AdminRetrieveAiKnowledgeDTO dto = AdminRetrieveAiKnowledgeDTO.builder()
                .knowledgeBaseIds(List.of(1L))
                .query("休假制度")
                .build();
        when(aiKnowledgeRetrievalFacade.adminRetrieve(dto))
                .thenReturn(AiKnowledgeRetrievalResultBO.builder()
                        .status(1)
                        .hitCount(1)
                        .hits(List.of(AiKnowledgeRetrievalHitBO.builder()
                                .documentName("员工手册.md")
                                .content("制度内容")
                                .build()))
                        .build());

        ApiResult<AiKnowledgeRetrievalResultBO> result = controller.retrieve(dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getHitCount()).isEqualTo(1);
        assertThat(result.getData().getHits().getFirst().getDocumentName()).isEqualTo("员工手册.md");
        verify(aiKnowledgeRetrievalFacade).adminRetrieve(dto);
    }

    @Test
    void listLogsReturnsPagedLogs() {
        PageResult<AiKnowledgeRetrievalLogBO> pageResult = new PageResult<AiKnowledgeRetrievalLogBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiKnowledgeRetrievalLogBO.builder()
                        .id(100L)
                        .query("休假制度")
                        .status(1)
                        .build()));
        when(aiKnowledgeRetrievalFacade.adminListLogs(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiKnowledgeRetrievalLogBO>> result = controller.listLogs(
                new PageParam(),
                new AdminListAiKnowledgeRetrievalLogDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getQuery()).isEqualTo("休假制度");
    }

    @Test
    void getLogByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiKnowledgeRetrievalFacade.getLogById(100L, true))
                .thenReturn(AiKnowledgeRetrievalLogBO.builder().id(100L).query("休假制度").build());

        ApiResult<AiKnowledgeRetrievalLogBO> result = controller.getLogById(100L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(100L);
        verify(aiKnowledgeRetrievalFacade).getLogById(100L, true);
    }

    @Test
    void endpointsUseExpectedPermissions() throws Exception {
        assertPermission("retrieve", "AiKnowledge:retrieve", AdminRetrieveAiKnowledgeDTO.class);
        assertPermission("listLogs", "AiKnowledge:retrieve", PageParam.class,
                AdminListAiKnowledgeRetrievalLogDTO.class);
        assertPermission("getLogById", "AiKnowledge:retrieve", Long.class);
    }

    private void assertPermission(String methodName, String value, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = AdminAiKnowledgeRetrievalController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        assertThat(permission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(permission.value()).containsExactly(value);
        assertThat(permission.orRole()).containsExactly("SuperAdmin");
    }
}
