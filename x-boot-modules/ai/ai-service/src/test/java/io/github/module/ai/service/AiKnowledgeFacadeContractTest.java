package io.github.module.ai.service;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiChatFacade;
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.facade.AiKnowledgeDocumentFacade;
import io.github.module.ai.facade.AiKnowledgeRetrievalFacade;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AdminAiChatBO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AiKnowledgeFacadeContractTest {

    @Test
    void chatContractShouldExposeRagKnowledgeBaseAndReferences() throws NoSuchMethodException {
        assertThat(AiChatFacade.class.getMethod("adminChat", AdminAiChatDTO.class).getReturnType())
                .isEqualTo(AdminAiChatBO.class);
        assertThat(AiChatFacade.class.getMethod("adminStream", AdminAiChatDTO.class).getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<io.github.module.ai.model.response.AdminAiChatStreamChunkBO>");
        assertThat(AdminAiChatDTO.class.getMethod("getKnowledgeBaseIds").getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<java.lang.Long>");
        assertThat(AdminAiChatBO.class.getMethod("getKnowledgeRetrievalLogId").getReturnType()).isEqualTo(Long.class);
        assertThat(AdminAiChatBO.class.getMethod("getReferences").getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO>");
        assertThat(AdminAiChatStreamChunkBO.class.getMethod("getKnowledgeRetrievalLogId").getReturnType()).isEqualTo(Long.class);
        assertThat(AdminAiChatStreamChunkBO.class.getMethod("getReferences").getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO>");
    }

    @Test
    void knowledgeBaseFacadeShouldExposeAdminContractOnly() throws NoSuchMethodException {
        assertThat(AiKnowledgeBaseFacade.class).isInterface();
        assertPageResult(AiKnowledgeBaseFacade.class.getMethod("adminList", PageParam.class, AdminListAiKnowledgeBaseDTO.class),
                AiKnowledgeBaseBO.class);
        assertThat(AiKnowledgeBaseFacade.class.getMethod("adminSelectOptions").getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<io.github.module.ai.model.response.AiKnowledgeBaseBO>");
        assertThat(AiKnowledgeBaseFacade.class.getMethod("getOneById", Long.class).getReturnType())
                .isEqualTo(AiKnowledgeBaseDetailBO.class);
        assertThrowsBusinessException(AiKnowledgeBaseFacade.class.getMethod("getOneById", Long.class, boolean.class));
        assertThat(AiKnowledgeBaseFacade.class.getMethod("adminInsert", AdminInsertOrUpdateAiKnowledgeBaseDTO.class).getReturnType())
                .isEqualTo(Long.class);
        assertThat(AiKnowledgeBaseFacade.class.getMethod("adminUpdate", AdminInsertOrUpdateAiKnowledgeBaseDTO.class).getReturnType())
                .isEqualTo(void.class);
        assertThat(AiKnowledgeBaseFacade.class.getMethod("adminDelete", Collection.class).getReturnType())
                .isEqualTo(void.class);
        assertThat(AiKnowledgeBaseFacade.class.getMethod("adminUpdateStatus", Long.class, Integer.class).getReturnType())
                .isEqualTo(void.class);
    }

    @Test
    void knowledgeDocumentFacadeShouldExposeDocumentAndChunkContract() throws NoSuchMethodException {
        assertThat(AiKnowledgeDocumentFacade.class).isInterface();
        assertPageResult(AiKnowledgeDocumentFacade.class.getMethod("adminList", PageParam.class, AdminListAiKnowledgeDocumentDTO.class),
                AiKnowledgeDocumentBO.class);
        assertThat(AiKnowledgeDocumentFacade.class.getMethod("getOneById", Long.class).getReturnType())
                .isEqualTo(AiKnowledgeDocumentDetailBO.class);
        assertThrowsBusinessException(AiKnowledgeDocumentFacade.class.getMethod("getOneById", Long.class, boolean.class));
        assertThat(AiKnowledgeDocumentFacade.class.getMethod("adminBindOssFile", AdminBindAiKnowledgeDocumentDTO.class).getReturnType())
                .isEqualTo(Long.class);
        assertThat(AiKnowledgeDocumentFacade.class.getMethod("adminDelete", Collection.class).getReturnType())
                .isEqualTo(void.class);
        assertThat(AiKnowledgeDocumentFacade.class.getMethod("adminRetry", Long.class).getReturnType())
                .isEqualTo(void.class);
        assertPageResult(AiKnowledgeDocumentFacade.class.getMethod("adminListChunks",
                Long.class,
                PageParam.class,
                AdminListAiKnowledgeDocumentChunkDTO.class), AiKnowledgeDocumentChunkBO.class);
    }

    @Test
    void knowledgeRetrievalFacadeShouldExposeRetrievalAndLogContract() throws NoSuchMethodException {
        assertThat(AiKnowledgeRetrievalFacade.class).isInterface();
        assertThat(AiKnowledgeRetrievalFacade.class.getMethod("adminRetrieve", AdminRetrieveAiKnowledgeDTO.class).getReturnType())
                .isEqualTo(AiKnowledgeRetrievalResultBO.class);
        assertPageResult(AiKnowledgeRetrievalFacade.class.getMethod("adminListLogs",
                PageParam.class,
                AdminListAiKnowledgeRetrievalLogDTO.class), AiKnowledgeRetrievalLogBO.class);
        assertThrowsBusinessException(AiKnowledgeRetrievalFacade.class.getMethod("getLogById", Long.class, boolean.class));
    }

    @Test
    void requestAndResponseModelsShouldStaySerializable() {
        Set<Class<?>> contractModels = Set.of(
                AdminAiChatDTO.class,
                AdminBindAiKnowledgeDocumentDTO.class,
                AdminInsertOrUpdateAiKnowledgeBaseDTO.class,
                AdminListAiKnowledgeBaseDTO.class,
                AdminListAiKnowledgeDocumentChunkDTO.class,
                AdminListAiKnowledgeDocumentDTO.class,
                AdminListAiKnowledgeRetrievalLogDTO.class,
                AdminRetrieveAiKnowledgeDTO.class,
                AdminAiChatBO.class,
                AdminAiChatStreamChunkBO.class,
                AiKnowledgeBaseBO.class,
                AiKnowledgeBaseDetailBO.class,
                AiKnowledgeDocumentBO.class,
                AiKnowledgeDocumentChunkBO.class,
                AiKnowledgeDocumentDetailBO.class,
                AiKnowledgeRetrievalHitBO.class,
                AiKnowledgeRetrievalLogBO.class,
                AiKnowledgeRetrievalResultBO.class
        );

        assertThat(contractModels).allMatch(Serializable.class::isAssignableFrom);
    }

    @Test
    void retrievalResultShouldReturnReferenceHitsForRagContext() throws NoSuchMethodException {
        Method getHits = AiKnowledgeRetrievalResultBO.class.getMethod("getHits");
        assertThat(getHits.getGenericReturnType().getTypeName())
                .isEqualTo("java.util.List<io.github.module.ai.model.response.AiKnowledgeRetrievalHitBO>");
        assertThat(AiKnowledgeRetrievalHitBO.class.getMethod("getKnowledgeBaseId").getReturnType()).isEqualTo(Long.class);
        assertThat(AiKnowledgeRetrievalHitBO.class.getMethod("getDocumentId").getReturnType()).isEqualTo(Long.class);
        assertThat(AiKnowledgeRetrievalHitBO.class.getMethod("getChunkId").getReturnType()).isEqualTo(Long.class);
        assertThat(AiKnowledgeRetrievalHitBO.class.getMethod("getContent").getReturnType()).isEqualTo(String.class);
        assertThat(AiKnowledgeRetrievalHitBO.class.getMethod("getSimilarityScore").getReturnType()).isEqualTo(Double.class);
    }

    private void assertPageResult(Method method, Class<?> recordType) {
        assertThat(method.getReturnType()).isEqualTo(PageResult.class);
        assertThat(method.getGenericReturnType()).isInstanceOf(ParameterizedType.class);
        ParameterizedType pageResultType = (ParameterizedType) method.getGenericReturnType();
        assertThat(pageResultType.getActualTypeArguments()).containsExactly(recordType);
    }

    private void assertThrowsBusinessException(Method method) {
        assertThat(method.getExceptionTypes()).containsExactly(BusinessException.class);
    }
}
