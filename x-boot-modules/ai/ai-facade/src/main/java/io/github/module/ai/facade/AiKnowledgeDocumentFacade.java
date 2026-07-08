package io.github.module.ai.facade;

import io.github.framework.core.exception.BusinessException;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.module.ai.model.request.AdminBindAiKnowledgeDocumentDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentChunkDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeDocumentDTO;
import io.github.module.ai.model.response.AiKnowledgeDocumentBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentChunkBO;
import io.github.module.ai.model.response.AiKnowledgeDocumentDetailBO;

import java.util.Collection;

/**
 * 后台 AI 知识库文档 Facade 接口.
 */
public interface AiKnowledgeDocumentFacade {

    /**
     * 后台管理-分页列表知识库文档.
     */
    PageResult<AiKnowledgeDocumentBO> adminList(PageParam pageParam, AdminListAiKnowledgeDocumentDTO dto);

    /**
     * 根据 ID 取知识库文档详情.
     */
    AiKnowledgeDocumentDetailBO getOneById(Long id);

    /**
     * 根据 ID 取知识库文档详情.
     */
    AiKnowledgeDocumentDetailBO getOneById(Long id, boolean throwIfInvalidId) throws BusinessException;

    /**
     * 后台管理-关联 OSS 文件为知识库文档.
     *
     * @return 主键ID
     */
    Long adminBindOssFile(AdminBindAiKnowledgeDocumentDTO dto);

    /**
     * 后台管理-删除知识库文档.
     */
    void adminDelete(Collection<Long> ids);

    /**
     * 后台管理-重试文档解析或切片.
     */
    void adminRetry(Long id);

    /**
     * 后台管理-分页列表文档切片.
     */
    PageResult<AiKnowledgeDocumentChunkBO> adminListChunks(Long documentId,
                                                           PageParam pageParam,
                                                           AdminListAiKnowledgeDocumentChunkDTO dto);
}
