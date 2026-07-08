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
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.model.request.AdminInsertOrUpdateAiKnowledgeBaseDTO;
import io.github.module.ai.model.request.AdminListAiKnowledgeBaseDTO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiKnowledgeBaseDetailBO;
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

import java.util.List;

@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-AI知识库接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiKnowledgeBaseController {

    private static final String PERMISSION_PREFIX = "AiKnowledge:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiKnowledgeBaseFacade aiKnowledgeBaseFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库分页列表")
    @GetMapping(value = "/ai/knowledge-bases")
    public ApiResult<PageResult<AiKnowledgeBaseBO>> list(PageParam pageParam, AdminListAiKnowledgeBaseDTO dto) {
        return ApiResult.data(aiKnowledgeBaseFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "启用知识库下拉框")
    @GetMapping(value = "/ai/knowledge-bases/options")
    public ApiResult<List<AiKnowledgeBaseBO>> options() {
        return ApiResult.data(aiKnowledgeBaseFacade.adminSelectOptions());
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库详情")
    @GetMapping(value = "/ai/knowledge-bases/{id}")
    public ApiResult<AiKnowledgeBaseDetailBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(aiKnowledgeBaseFacade.getOneById(id, true));
    }

    @SysLog(value = "新增AI知识库")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "新增知识库")
    @PostMapping(value = "/ai/knowledge-bases")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        aiKnowledgeBaseFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑AI知识库")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE, orRole = "SuperAdmin")
    @Operation(summary = "编辑知识库")
    @PutMapping(value = "/ai/knowledge-bases/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody @Valid AdminInsertOrUpdateAiKnowledgeBaseDTO dto) {
        dto.setId(id);
        aiKnowledgeBaseFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI知识库")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除知识库")
    @DeleteMapping(value = "/ai/knowledge-bases")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        aiKnowledgeBaseFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "更新AI知识库启停状态")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "enable", orRole = "SuperAdmin")
    @Operation(summary = "启停知识库")
    @PutMapping(value = "/ai/knowledge-bases/{id}/status/{status}")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @PathVariable("status") Integer status) {
        aiKnowledgeBaseFacade.adminUpdateStatus(id, status);

        return ApiResult.success();
    }
}
