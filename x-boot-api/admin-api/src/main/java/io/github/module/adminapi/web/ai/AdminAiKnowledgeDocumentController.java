package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
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
import io.github.module.sys.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-AI知识库文档接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiKnowledgeDocumentController {

    private static final String PERMISSION_PREFIX = "AiKnowledge:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiKnowledgeDocumentFacade aiKnowledgeDocumentFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库文档分页列表")
    @GetMapping(value = "/ai/knowledge-documents")
    public ApiResult<PageResult<AiKnowledgeDocumentBO>> list(PageParam pageParam,
                                                             AdminListAiKnowledgeDocumentDTO dto) {
        return ApiResult.data(aiKnowledgeDocumentFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库文档详情")
    @GetMapping(value = "/ai/knowledge-documents/{id}")
    public ApiResult<AiKnowledgeDocumentDetailBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(aiKnowledgeDocumentFacade.getOneById(id, true));
    }

    @SysLog(value = "关联OSS文件为AI知识库文档")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "关联 OSS 文件为知识库文档")
    @PostMapping(value = "/ai/knowledge-documents/oss-file-bindings")
    public ApiResult<Void> bindOssFile(@RequestBody @Valid AdminBindAiKnowledgeDocumentDTO dto) {
        aiKnowledgeDocumentFacade.adminBindOssFile(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI知识库文档")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除知识库文档")
    @DeleteMapping(value = "/ai/knowledge-documents")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        aiKnowledgeDocumentFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "重试AI知识库文档解析或切片")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "retry", orRole = "SuperAdmin")
    @Operation(summary = "重试知识库文档解析或切片")
    @PutMapping(value = "/ai/knowledge-documents/{id}/retry")
    public ApiResult<Void> retry(@PathVariable("id") Long id) {
        aiKnowledgeDocumentFacade.adminRetry(id);

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库文档切片分页列表")
    @GetMapping(value = "/ai/knowledge-documents/{documentId}/chunks")
    public ApiResult<PageResult<AiKnowledgeDocumentChunkBO>> chunks(@PathVariable("documentId") Long documentId,
                                                                    PageParam pageParam,
                                                                    AdminListAiKnowledgeDocumentChunkDTO dto) {
        return ApiResult.data(aiKnowledgeDocumentFacade.adminListChunks(documentId, pageParam, dto));
    }
}
