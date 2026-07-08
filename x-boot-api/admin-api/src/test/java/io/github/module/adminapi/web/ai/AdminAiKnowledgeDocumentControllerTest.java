package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiKnowledgeDocumentFacade;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;
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
class AdminAiKnowledgeDocumentControllerTest {

    @Mock
    private AiKnowledgeDocumentFacade aiKnowledgeDocumentFacade;

    private AdminAiKnowledgeDocumentController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminAiKnowledgeDocumentController();
        ReflectionTestUtils.setField(controller, "aiKnowledgeDocumentFacade", aiKnowledgeDocumentFacade);
    }

    @Test
    void listReturnsPagedDocuments() {
        PageResult<AiKnowledgeDocumentBO> pageResult = new PageResult<AiKnowledgeDocumentBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiKnowledgeDocumentBO.builder()
                        .id(1L)
                        .documentName("制度文档.pdf")
                        .parseStatus(0)
                        .chunkStatus(0)
                        .errorMessage("PDF解析失败")
                        .build()));
        when(aiKnowledgeDocumentFacade.adminList(any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiKnowledgeDocumentBO>> result = controller.list(
                new PageParam(),
                new AdminListAiKnowledgeDocumentDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getTotal()).isEqualTo(1);
        assertThat(result.getData().getRecords().getFirst().getErrorMessage()).isEqualTo("PDF解析失败");
    }

    @Test
    void getByIdCallsFacadeWithThrowIfInvalidId() {
        when(aiKnowledgeDocumentFacade.getOneById(7L, true))
                .thenReturn(AiKnowledgeDocumentDetailBO.builder().id(7L).documentName("制度文档.pdf").build());

        ApiResult<AiKnowledgeDocumentDetailBO> result = controller.getById(7L);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getId()).isEqualTo(7L);
        verify(aiKnowledgeDocumentFacade).getOneById(7L, true);
    }

    @Test
    void bindOssFileCallsFacade() {
        AdminBindAiKnowledgeDocumentDTO dto = AdminBindAiKnowledgeDocumentDTO.builder()
                .knowledgeBaseId(1L)
                .ossFileId(9L)
                .build();

        ApiResult<Void> result = controller.bindOssFile(dto);

        assertThat(result.getCode()).isEqualTo(200);
        verify(aiKnowledgeDocumentFacade).adminBindOssFile(dto);
    }

    @Test
    void deletePassesIdsToFacade() {
        controller.delete(new IdsDTO<Long>().setIds(List.of(1L, 2L)));

        verify(aiKnowledgeDocumentFacade).adminDelete(List.of(1L, 2L));
    }

    @Test
    void retryPassesIdToFacade() {
        ApiResult<Void> result = controller.retry(1L);

        assertThat(result.getCode()).isEqualTo(200);
        verify(aiKnowledgeDocumentFacade).adminRetry(1L);
    }

    @Test
    void chunksReturnsPagedDocumentChunks() {
        PageResult<AiKnowledgeDocumentChunkBO> pageResult = new PageResult<AiKnowledgeDocumentChunkBO>()
                .setCurrent(1)
                .setSize(10)
                .setTotal(1)
                .setRecords(List.of(AiKnowledgeDocumentChunkBO.builder()
                        .id(1L)
                        .documentName("制度文档.pdf")
                        .contentPreview("制度内容")
                        .build()));
        when(aiKnowledgeDocumentFacade.adminListChunks(any(), any(), any())).thenReturn(pageResult);

        ApiResult<PageResult<AiKnowledgeDocumentChunkBO>> result = controller.chunks(
                1L,
                new PageParam(),
                new AdminListAiKnowledgeDocumentChunkDTO()
        );

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getData().getRecords().getFirst().getDocumentName()).isEqualTo("制度文档.pdf");
    }

    @Test
    void endpointsUseExpectedPermissions() throws Exception {
        assertPermission("list", "AiKnowledge:retrieve", PageParam.class, AdminListAiKnowledgeDocumentDTO.class);
        assertPermission("getById", "AiKnowledge:retrieve", Long.class);
        assertPermission("bindOssFile", "AiKnowledge:create", AdminBindAiKnowledgeDocumentDTO.class);
        assertPermission("delete", "AiKnowledge:delete", IdsDTO.class);
        assertPermission("retry", "AiKnowledge:retry", Long.class);
        assertPermission(
                "chunks",
                "AiKnowledge:retrieve",
                Long.class,
                PageParam.class,
                AdminListAiKnowledgeDocumentChunkDTO.class
        );
    }

    private void assertPermission(String methodName, String value, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method = AdminAiKnowledgeDocumentController.class.getDeclaredMethod(methodName, parameterTypes);
        SaCheckPermission permission = method.getAnnotation(SaCheckPermission.class);
        assertThat(permission).isNotNull();
        assertThat(permission.type()).isEqualTo(AdminStpUtil.TYPE);
        assertThat(permission.value()).containsExactly(value);
        assertThat(permission.orRole()).containsExactly("SuperAdmin");
    }
}
