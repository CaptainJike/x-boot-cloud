package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * 后台管理-新增/编辑工作流.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminInsertOrUpdateAiWorkflowDTO implements Serializable {

    @Schema(description = "主键ID", hidden = true, title = "仅更新时使用")
    private Long id;

    @Schema(description = "工作流编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 64, message = "【工作流编码】最长64位")
    @NotBlank(message = "工作流编码不能为空")
    private String workflowCode;

    @Schema(description = "工作流名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 100, message = "【工作流名称】最长100位")
    @NotBlank(message = "工作流名称不能为空")
    private String name;

    @Schema(description = "工作流描述")
    @Size(max = 500, message = "【工作流描述】最长500位")
    private String description;

    @Schema(description = "关联Agent ID")
    private Long agentId;

    @Schema(description = "版本号")
    @Min(value = 1, message = "【版本号】不能小于1")
    private Integer versionNo;

    @Schema(description = "入口节点Key")
    @Size(max = 64, message = "【入口节点Key】最长64位")
    private String entryNodeKey;

    @Schema(description = "定义快照JSON")
    @Size(max = 8000, message = "【定义快照JSON】最长8000位")
    private String definitionSnapshot;

    @Schema(description = "状态(0=禁用 1=启用)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;
}
