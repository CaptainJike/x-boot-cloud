package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-新增/编辑工作流节点.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminInsertOrUpdateAiWorkflowNodeDTO implements Serializable {

    @Schema(description = "主键ID", hidden = true, title = "仅更新时使用")
    private Long id;

    @Schema(description = "工作流定义ID", hidden = true)
    @NotNull(message = "工作流定义ID不能为空")
    private Long workflowDefinitionId;

    @Schema(description = "节点Key", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 64, message = "【节点Key】最长64位")
    @NotBlank(message = "节点Key不能为空")
    private String nodeKey;

    @Schema(description = "节点名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【节点名称】最长100位")
    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    @Schema(description = "节点类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 32, message = "【节点类型】最长32位")
    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    @Schema(description = "节点描述")
    @Size(max = 500, message = "【节点描述】最长500位")
    private String description;

    @Schema(description = "节点配置JSON")
    @Size(max = 8000, message = "【节点配置JSON】最长8000位")
    private String nodeConfig;

    @Schema(description = "输入映射JSON")
    @Size(max = 4000, message = "【输入映射JSON】最长4000位")
    private String inputMapping;

    @Schema(description = "输出映射JSON")
    @Size(max = 4000, message = "【输出映射JSON】最长4000位")
    private String outputMapping;

    @Schema(description = "下游节点Key列表")
    @Size(max = 2000, message = "【下游节点Key列表】最长2000位")
    private String nextNodeKeys;

    @Schema(description = "条件表达式")
    @Size(max = 1000, message = "【条件表达式】最长1000位")
    private String conditionExpression;

    @Schema(description = "错误策略")
    @Size(max = 64, message = "【错误策略】最长64位")
    private String errorStrategy;

    @Schema(description = "重试次数")
    @Min(value = 0, message = "【重试次数】不能小于0")
    @Max(value = 10, message = "【重试次数】不能大于10")
    private Integer retryCount;

    @Schema(description = "超时时间，单位秒")
    @Min(value = 1, message = "【超时时间】不能小于1秒")
    @Max(value = 3600, message = "【超时时间】不能大于3600秒")
    private Long timeoutSeconds;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
