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
@Tag(name = "后台管理-AI工作流接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiWorkflowController {

    private static final String PERMISSION_PREFIX = "AiWorkflow:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiWorkflowFacade aiWorkflowFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流分页列表")
    @GetMapping(value = "/ai/workflows")
    public ApiResult<PageResult<AiWorkflowBO>> list(PageParam pageParam, AdminListAiWorkflowDTO dto) {
        return ApiResult.data(aiWorkflowFacade.adminList(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "启用工作流下拉框")
    @GetMapping(value = "/ai/workflows/options")
    public ApiResult<List<AiWorkflowBO>> options() {
        return ApiResult.data(aiWorkflowFacade.adminSelectOptions());
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流详情")
    @GetMapping(value = "/ai/workflows/{id}")
    public ApiResult<AiWorkflowDetailBO> getById(@PathVariable("id") Long id) {
        return ApiResult.data(aiWorkflowFacade.getOneById(id, true));
    }

    @SysLog(value = "新增AI工作流")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "新增工作流")
    @PostMapping(value = "/ai/workflows")
    public ApiResult<Void> insert(@RequestBody @Valid AdminInsertOrUpdateAiWorkflowDTO dto) {
        aiWorkflowFacade.adminInsert(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑AI工作流")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE, orRole = "SuperAdmin")
    @Operation(summary = "编辑工作流")
    @PutMapping(value = "/ai/workflows/{id}")
    public ApiResult<Void> update(@PathVariable("id") Long id,
                                  @RequestBody @Valid AdminInsertOrUpdateAiWorkflowDTO dto) {
        dto.setId(id);
        aiWorkflowFacade.adminUpdate(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI工作流")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除工作流")
    @DeleteMapping(value = "/ai/workflows")
    public ApiResult<Void> delete(@RequestBody @Valid IdsDTO<Long> dto) {
        aiWorkflowFacade.adminDelete(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "更新AI工作流启停状态")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "enable", orRole = "SuperAdmin")
    @Operation(summary = "启停工作流")
    @PutMapping(value = "/ai/workflows/{id}/status/{status}")
    public ApiResult<Void> updateStatus(@PathVariable("id") Long id,
                                        @PathVariable("status") Integer status) {
        aiWorkflowFacade.adminUpdateStatus(id, status);

        return ApiResult.success();
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流节点列表")
    @GetMapping(value = "/ai/workflows/{workflowDefinitionId}/nodes")
    public ApiResult<List<AiWorkflowNodeBO>> listNodes(@PathVariable("workflowDefinitionId") Long workflowDefinitionId,
                                                       AdminListAiWorkflowNodeDTO dto) {
        dto.setWorkflowDefinitionId(workflowDefinitionId);
        return ApiResult.data(aiWorkflowFacade.adminListNodes(dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流节点详情")
    @GetMapping(value = "/ai/workflow-nodes/{id}")
    public ApiResult<AiWorkflowNodeDetailBO> getNodeById(@PathVariable("id") Long id) {
        return ApiResult.data(aiWorkflowFacade.getNodeById(id, true));
    }

    @SysLog(value = "新增AI工作流节点")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.CREATE, orRole = "SuperAdmin")
    @Operation(summary = "新增工作流节点")
    @PostMapping(value = "/ai/workflows/{workflowDefinitionId}/nodes")
    public ApiResult<Void> insertNode(@PathVariable("workflowDefinitionId") Long workflowDefinitionId,
                                      @RequestBody @Valid AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        dto.setWorkflowDefinitionId(workflowDefinitionId);
        aiWorkflowFacade.adminInsertNode(dto);

        return ApiResult.success();
    }

    @SysLog(value = "编辑AI工作流节点")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.UPDATE, orRole = "SuperAdmin")
    @Operation(summary = "编辑工作流节点")
    @PutMapping(value = "/ai/workflows/{workflowDefinitionId}/nodes/{id}")
    public ApiResult<Void> updateNode(@PathVariable("workflowDefinitionId") Long workflowDefinitionId,
                                      @PathVariable("id") Long id,
                                      @RequestBody @Valid AdminInsertOrUpdateAiWorkflowNodeDTO dto) {
        dto.setId(id);
        dto.setWorkflowDefinitionId(workflowDefinitionId);
        aiWorkflowFacade.adminUpdateNode(dto);

        return ApiResult.success();
    }

    @SysLog(value = "删除AI工作流节点")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.DELETE, orRole = "SuperAdmin")
    @Operation(summary = "删除工作流节点")
    @DeleteMapping(value = "/ai/workflow-nodes")
    public ApiResult<Void> deleteNodes(@RequestBody @Valid IdsDTO<Long> dto) {
        aiWorkflowFacade.adminDeleteNodes(dto.getIds());

        return ApiResult.success();
    }

    @SysLog(value = "执行AI工作流")
    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + "execute", orRole = "SuperAdmin")
    @Operation(summary = "执行工作流")
    @PostMapping(value = "/ai/workflows/{workflowDefinitionId}:execute")
    public ApiResult<AdminAiWorkflowExecutionResultBO> execute(
            @PathVariable("workflowDefinitionId") Long workflowDefinitionId,
            @RequestBody @Valid AdminExecuteAiWorkflowDTO dto
    ) {
        dto.setWorkflowDefinitionId(workflowDefinitionId);
        return ApiResult.data(aiWorkflowFacade.adminExecute(workflowDefinitionId, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流执行记录分页列表")
    @GetMapping(value = "/ai/workflow-executions")
    public ApiResult<PageResult<AdminAiWorkflowExecutionBO>> listExecutions(
            PageParam pageParam,
            AdminListAiWorkflowExecutionDTO dto
    ) {
        return ApiResult.data(aiWorkflowFacade.adminListExecutions(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "工作流执行记录详情")
    @GetMapping(value = "/ai/workflow-executions/{id}")
    public ApiResult<AdminAiWorkflowExecutionDetailBO> getExecutionById(@PathVariable("id") Long id) {
        return ApiResult.data(aiWorkflowFacade.getExecutionById(id, true));
    }
}
