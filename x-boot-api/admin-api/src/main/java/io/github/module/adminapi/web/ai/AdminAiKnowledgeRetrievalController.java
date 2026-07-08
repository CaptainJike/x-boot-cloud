package io.github.module.adminapi.web.ai;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.page.PageParam;
import io.github.framework.core.page.PageResult;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.facade.AiKnowledgeRetrievalFacade;
import io.github.module.ai.model.request.AdminListAiKnowledgeRetrievalLogDTO;
import io.github.module.ai.model.request.AdminRetrieveAiKnowledgeDTO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalLogBO;
import io.github.module.ai.model.response.AiKnowledgeRetrievalResultBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-AI知识库检索接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminAiKnowledgeRetrievalController {

    private static final String PERMISSION_PREFIX = "AiKnowledge:";

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiKnowledgeRetrievalFacade aiKnowledgeRetrievalFacade;

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "执行知识库基础检索")
    @PostMapping(value = "/ai/knowledge-retrievals")
    public ApiResult<AiKnowledgeRetrievalResultBO> retrieve(@RequestBody @Valid AdminRetrieveAiKnowledgeDTO dto) {
        return ApiResult.data(aiKnowledgeRetrievalFacade.adminRetrieve(dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库检索日志分页列表")
    @GetMapping(value = "/ai/knowledge-retrieval-logs")
    public ApiResult<PageResult<AiKnowledgeRetrievalLogBO>> listLogs(PageParam pageParam,
                                                                     AdminListAiKnowledgeRetrievalLogDTO dto) {
        return ApiResult.data(aiKnowledgeRetrievalFacade.adminListLogs(pageParam, dto));
    }

    @SaCheckPermission(type = AdminStpUtil.TYPE, value = PERMISSION_PREFIX + BaseConstant.Permission.RETRIEVE, orRole = "SuperAdmin")
    @Operation(summary = "知识库检索日志详情")
    @GetMapping(value = "/ai/knowledge-retrieval-logs/{id}")
    public ApiResult<AiKnowledgeRetrievalLogBO> getLogById(@PathVariable("id") Long id) {
        return ApiResult.data(aiKnowledgeRetrievalFacade.getLogById(id, true));
    }
}
