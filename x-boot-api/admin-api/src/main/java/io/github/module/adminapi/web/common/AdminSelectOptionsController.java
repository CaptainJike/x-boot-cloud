package io.github.module.adminapi.web.common;


import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.framework.core.constant.ApiPrefixConstant;
import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.core.enums.GenderEnum;
import io.github.framework.core.enums.YesOrNoEnum;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.model.response.AdminSelectOptionItemVO;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.ai.constant.AiModelCapabilityConstant;
import io.github.module.ai.facade.AiAgentFacade;
import io.github.module.ai.facade.AiKnowledgeBaseFacade;
import io.github.module.ai.facade.AiModelConfigFacade;
import io.github.module.ai.facade.AiWorkflowFacade;
import io.github.module.ai.model.response.AiAgentBO;
import io.github.module.ai.model.response.AiKnowledgeBaseBO;
import io.github.module.ai.model.response.AiModelConfigBO;
import io.github.module.ai.model.response.AiWorkflowBO;
import io.github.module.sys.enums.SysLogStatusEnum;
import io.github.module.sys.enums.SysMenuTypeEnum;
import io.github.module.sys.enums.SysUserStatusEnum;
import io.github.module.sys.facade.SysDataDictFacade;
import io.github.module.sys.facade.SysDeptFacade;
import io.github.module.sys.facade.SysMenuFacade;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.response.SysDataDictItemBO;
import io.github.module.sys.model.response.SysDeptBO;
import io.github.module.sys.model.response.SysMenuBO;
import io.github.module.sys.model.response.SysRoleBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

// 约束：登录后才能使用   👇 后台管理对应的鉴权工具类
@SaCheckLogin(type = AdminStpUtil.TYPE)
@Tag(name = "后台管理-下拉框数据源接口")
@RequestMapping(ApiPrefixConstant.API_PREFIX_ADMIN + ApiPrefixConstant.VERSION)
@RequiredArgsConstructor
@RestController
@Slf4j
public class AdminSelectOptionsController {

    private static final Set<String> KNOWLEDGE_EMBEDDING_PROVIDER_TYPES =
            Set.of("OPENAI", "OPENAI_COMPATIBLE", "DEEPSEEK", "ZHIPU");

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysRoleFacade sysRoleFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysDeptFacade sysDeptFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysMenuFacade sysMenuFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private SysDataDictFacade sysDataDictFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiModelConfigFacade aiModelConfigFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiKnowledgeBaseFacade aiKnowledgeBaseFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiAgentFacade aiAgentFacade;

    @DubboReference(version = BaseConstant.Version.DUBBO_VERSION_V1, validation = BaseConstant.Dubbo.ENABLE_VALIDATION)
    private AiWorkflowFacade aiWorkflowFacade;


    /*
    这里统一存放所有用于后台管理的下拉框数据源接口
    避免多人协作时，不知道原来是否已经有了，或者写在某个边边角角里，造成重复开发
    */

    @Operation(summary = "后台角色下拉框")
    @GetMapping(value = "/select-options/roles")
    public ApiResult<List<AdminSelectOptionItemVO>> roles() {
        return ApiResult.data(sysRoleFacade.adminSelectOptions().stream()
                .map(this::roleOption)
                .toList());
    }

    @Operation(summary = "部门下拉框（前端负责转为树状数据）")
    @GetMapping(value = "/select-options/depts")
    public ApiResult<List<AdminSelectOptionItemVO>> depts() {
        return ApiResult.data(sysDeptFacade.adminSelectOptions(true).stream()
                .map(this::deptOption)
                .toList());
    }

    @Operation(summary = "后台菜单下拉框（前端负责转为树状数据）")
    @GetMapping(value = "/select-options/menus")
    public ApiResult<List<AdminSelectOptionItemVO>> menus() {
        return ApiResult.data(sysMenuFacade.adminListVisibleMenu().stream()
                .map(this::menuOption)
                .toList());
    }

    @Operation(summary = "数据字典项下拉框")
    @GetMapping(value = "/select-options/data-dict-items/{classifiedCode}")
    public ApiResult<List<AdminSelectOptionItemVO>> dataDictItems(
            @Parameter(description = "数据字典分类编码") @PathVariable("classifiedCode") String classifiedCode
    ) {
        return ApiResult.data(sysDataDictFacade.listEnabledItemsByClassifiedCode(classifiedCode).stream()
                .map(this::dataDictItemOption)
                .toList());
    }

    @Operation(summary = "AI模型配置下拉框（value=配置编码）")
    @GetMapping(value = "/select-options/ai-model-configs")
    public ApiResult<List<AdminSelectOptionItemVO>> aiModelConfigs() {
        return ApiResult.data(aiModelConfigFacade.adminSelectOptions().stream()
                .filter(item -> supportsCapability(item, AiModelCapabilityConstant.CHAT))
                .map(this::aiModelConfigOption)
                .toList());
    }

    @Operation(summary = "AI知识库向量化模型配置下拉框（value=配置编码）")
    @GetMapping(value = "/select-options/ai-embedding-model-configs")
    public ApiResult<List<AdminSelectOptionItemVO>> aiEmbeddingModelConfigs() {
        return ApiResult.data(aiModelConfigFacade.adminSelectOptions().stream()
                .filter(item -> supportsCapability(item, AiModelCapabilityConstant.EMBEDDING))
                .filter(item -> KNOWLEDGE_EMBEDDING_PROVIDER_TYPES.contains(item.getProviderType()))
                .map(this::aiModelConfigOption)
                .toList());
    }

    @Operation(summary = "AI知识库下拉框")
    @GetMapping(value = "/select-options/ai-knowledge-bases")
    public ApiResult<List<AdminSelectOptionItemVO>> aiKnowledgeBases() {
        return ApiResult.data(aiKnowledgeBaseFacade.adminSelectOptions().stream()
                .map(this::aiKnowledgeBaseOption)
                .toList());
    }

    @Operation(summary = "AI Agent下拉框")
    @GetMapping(value = "/select-options/ai-agents")
    public ApiResult<List<AdminSelectOptionItemVO>> aiAgents() {
        return ApiResult.data(aiAgentFacade.adminSelectOptions().stream()
                .map(this::aiAgentOption)
                .toList());
    }

    @Operation(summary = "AI工作流下拉框")
    @GetMapping(value = "/select-options/ai-workflows")
    public ApiResult<List<AdminSelectOptionItemVO>> aiWorkflows() {
        return ApiResult.data(aiWorkflowFacade.adminSelectOptions().stream()
                .map(this::aiWorkflowOption)
                .toList());
    }

    @Operation(summary = "启停状态下拉框")
    @GetMapping(value = "/select-options/enabled-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> enabledStatuses() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(EnabledStatusEnum.class));
    }

    @Operation(summary = "是否下拉框")
    @GetMapping(value = "/select-options/yes-or-no")
    public ApiResult<List<AdminSelectOptionItemVO>> yesOrNo() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(YesOrNoEnum.class));
    }

    @Operation(summary = "性别下拉框")
    @GetMapping(value = "/select-options/genders")
    public ApiResult<List<AdminSelectOptionItemVO>> genders() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(GenderEnum.class));
    }

    @Operation(summary = "后台用户状态下拉框")
    @GetMapping(value = "/select-options/sys-user-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> sysUserStatuses() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(SysUserStatusEnum.class));
    }

    @Operation(summary = "系统日志状态下拉框")
    @GetMapping(value = "/select-options/sys-log-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> sysLogStatuses() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(SysLogStatusEnum.class));
    }

    @Operation(summary = "菜单类型下拉框")
    @GetMapping(value = "/select-options/sys-menu-types")
    public ApiResult<List<AdminSelectOptionItemVO>> sysMenuTypes() {
        return ApiResult.data(AdminSelectOptionItemVO.listOf(SysMenuTypeEnum.class));
    }

    @Operation(summary = "AI供应商类型下拉框")
    @GetMapping(value = "/select-options/ai-provider-types")
    public ApiResult<List<AdminSelectOptionItemVO>> aiProviderTypes() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf("OPENAI", "OpenAI").setCode("OPENAI"),
                AdminSelectOptionItemVO.valueOf("OPENAI_COMPATIBLE", "OpenAI兼容接口").setCode("OPENAI_COMPATIBLE"),
                AdminSelectOptionItemVO.valueOf("DEEPSEEK", "DeepSeek").setCode("DEEPSEEK"),
                AdminSelectOptionItemVO.valueOf("ZHIPU", "智谱AI").setCode("ZHIPU"),
                AdminSelectOptionItemVO.valueOf("OLLAMA", "Ollama").setCode("OLLAMA")
        ));
    }

    @Operation(summary = "AI模型模态下拉框")
    @GetMapping(value = "/select-options/ai-model-modalities")
    public ApiResult<List<AdminSelectOptionItemVO>> aiModelModalities() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf("text", "文本"),
                AdminSelectOptionItemVO.valueOf("image", "图片")
        ));
    }

    @Operation(summary = "AI消息角色下拉框")
    @GetMapping(value = "/select-options/ai-message-roles")
    public ApiResult<List<AdminSelectOptionItemVO>> aiMessageRoles() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf("user", "用户"),
                AdminSelectOptionItemVO.valueOf("assistant", "助手"),
                AdminSelectOptionItemVO.valueOf("system", "系统")
        ));
    }

    @Operation(summary = "AI附件类型下拉框")
    @GetMapping(value = "/select-options/ai-attachment-types")
    public ApiResult<List<AdminSelectOptionItemVO>> aiAttachmentTypes() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf("image", "图片"),
                AdminSelectOptionItemVO.valueOf("file", "文件")
        ));
    }

    @Operation(summary = "AI工作流节点类型下拉框")
    @GetMapping(value = "/select-options/ai-workflow-node-types")
    public ApiResult<List<AdminSelectOptionItemVO>> aiWorkflowNodeTypes() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf("llm", "LLM"),
                AdminSelectOptionItemVO.valueOf("http", "HTTP"),
                AdminSelectOptionItemVO.valueOf("http_tool", "HTTP工具"),
                AdminSelectOptionItemVO.valueOf("condition", "条件"),
                AdminSelectOptionItemVO.valueOf("end", "结束"),
                AdminSelectOptionItemVO.valueOf("end_node", "结束节点")
        ));
    }

    @Operation(summary = "AI发布状态下拉框")
    @GetMapping(value = "/select-options/ai-publish-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> aiPublishStatuses() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf(0, "草稿"),
                AdminSelectOptionItemVO.valueOf(1, "已发布")
        ));
    }

    @Operation(summary = "AI处理状态下拉框")
    @GetMapping(value = "/select-options/ai-process-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> aiProcessStatuses() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf(0, "失败"),
                AdminSelectOptionItemVO.valueOf(1, "成功"),
                AdminSelectOptionItemVO.valueOf(2, "处理中"),
                AdminSelectOptionItemVO.valueOf(3, "待处理")
        ));
    }

    @Operation(summary = "AI结果状态下拉框")
    @GetMapping(value = "/select-options/ai-result-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> aiResultStatuses() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf(0, "失败"),
                AdminSelectOptionItemVO.valueOf(1, "成功")
        ));
    }

    @Operation(summary = "AI消息状态下拉框")
    @GetMapping(value = "/select-options/ai-message-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> aiMessageStatuses() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf(0, "失败"),
                AdminSelectOptionItemVO.valueOf(1, "成功"),
                AdminSelectOptionItemVO.valueOf(2, "生成中")
        ));
    }

    @Operation(summary = "AI会话状态下拉框")
    @GetMapping(value = "/select-options/ai-conversation-statuses")
    public ApiResult<List<AdminSelectOptionItemVO>> aiConversationStatuses() {
        return ApiResult.data(List.of(
                AdminSelectOptionItemVO.valueOf(0, "归档"),
                AdminSelectOptionItemVO.valueOf(1, "活跃")
        ));
    }

    private AdminSelectOptionItemVO roleOption(SysRoleBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getTitle())
                .setValue(item.getId())
                .setLabel(item.getTitle())
                .setCode(item.getValue());
    }

    private AdminSelectOptionItemVO deptOption(SysDeptBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getTitle(), item.getParentId())
                .setValue(item.getId())
                .setLabel(item.getTitle());
    }

    private AdminSelectOptionItemVO menuOption(SysMenuBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getTitle(), item.getParentId())
                .setValue(item.getId())
                .setLabel(item.getTitle())
                .setCode(item.getPermission());
    }

    private AdminSelectOptionItemVO dataDictItemOption(SysDataDictItemBO item) {
        return AdminSelectOptionItemVO.valueOf(item.getValue(), item.getLabel())
                .setCode(item.getCode())
                .setDescription(item.getDescription());
    }

    private AdminSelectOptionItemVO aiModelConfigOption(AiModelConfigBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getName())
                .setValue(item.getCode())
                .setLabel(optionLabel(item.getName(), item.getCode(), item.getModelName()))
                .setCode(item.getCode())
                .setProviderType(item.getProviderType())
                .setModelName(item.getModelName())
                .setSupportedModalities(item.getSupportedModalities())
                .setSupportedCapabilities(item.getSupportedCapabilities())
                .setDescription(item.getDescription());
    }

    private AdminSelectOptionItemVO aiKnowledgeBaseOption(AiKnowledgeBaseBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getName())
                .setValue(item.getId())
                .setLabel(optionLabel(item.getName(), item.getEmbeddingModelConfigCode(), null))
                .setCode(item.getEmbeddingModelConfigCode())
                .setDescription(item.getDescription());
    }

    private AdminSelectOptionItemVO aiAgentOption(AiAgentBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getName())
                .setValue(item.getId())
                .setLabel(optionLabel(item.getName(), item.getAgentCode(), item.getModelName()))
                .setCode(item.getAgentCode())
                .setProviderType(item.getProviderType())
                .setModelName(item.getModelName())
                .setDescription(item.getDescription());
    }

    private AdminSelectOptionItemVO aiWorkflowOption(AiWorkflowBO item) {
        return new AdminSelectOptionItemVO(item.getId(), item.getName())
                .setValue(item.getId())
                .setLabel(optionLabel(item.getName(), item.getWorkflowCode(), item.getVersionNo() == null ? null : "v" + item.getVersionNo()))
                .setCode(item.getWorkflowCode())
                .setDescription(item.getDescription());
    }

    private String optionLabel(String name, String code, String suffix) {
        StringBuilder label = new StringBuilder(name == null ? "" : name);
        if (code != null && !code.isBlank()) {
            label.append("（").append(code);
            if (suffix != null && !suffix.isBlank()) {
                label.append(" / ").append(suffix);
            }
            label.append("）");
        } else if (suffix != null && !suffix.isBlank()) {
            label.append("（").append(suffix).append("）");
        }
        return label.toString();
    }

    private boolean supportsCapability(AiModelConfigBO item, String capability) {
        return AiModelCapabilityConstant.contains(item == null ? null : item.getSupportedCapabilities(), capability);
    }
}
