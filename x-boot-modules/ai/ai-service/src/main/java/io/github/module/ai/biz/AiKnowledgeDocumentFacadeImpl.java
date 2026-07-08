package io.github.module.ai.biz;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.facade.AiKnowledgeDocumentFacade;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;
import io.github.module.ai.service.AiKnowledgeDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;

import java.util.Collection;

/**
 * 后台 AI 知识库文档 Facade 实现.
 */
@DubboService(
        version = BaseConstant.Version.DUBBO_VERSION_V1,
        validation = BaseConstant.Dubbo.ENABLE_VALIDATION,
        timeout = BaseConstant.Dubbo.TIMEOUT
)
@RequiredArgsConstructor
@Slf4j
public class AiKnowledgeDocumentFacadeImpl implements AiKnowledgeDocumentFacade {

    private final AiKnowledgeDocumentService aiKnowledgeDocumentService;

    @Override
    public PageResult<AiKnowledgeDocumentBO> adminList(PageParam pageParam, AdminListAiKnowledgeDocumentDTO dto) {
        return aiKnowledgeDocumentService.adminList(pageParam, dto);
    }

    @Override
    public AiKnowledgeDocumentDetailBO getOneById(Long id) {
        return aiKnowledgeDocumentService.getOneById(id);
    }

    @Override
    public AiKnowledgeDocumentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException {
        return aiKnowledgeDocumentService.getOneById(id, throwIfInvalidId);
    }

    @Override
    public Long adminBindOssFile(AdminBindAiKnowledgeDocumentDTO dto) {
        return aiKnowledgeDocumentService.adminBindOssFile(dto);
    }

    @Override
    public void adminDelete(Collection<Long> ids) {
        aiKnowledgeDocumentService.adminDelete(ids);
    }

    @Override
    public void adminRetry(Long id) {
        aiKnowledgeDocumentService.adminRetry(id);
    }

    @Override
    public PageResult<AiKnowledgeDocumentChunkBO> adminListChunks(Long documentId,
                                                                  PageParam pageParam,
                                                                  AdminListAiKnowledgeDocumentChunkDTO dto) {
        return aiKnowledgeDocumentService.adminListChunks(documentId, pageParam, dto);
    }
}
