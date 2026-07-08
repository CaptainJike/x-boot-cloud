package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-工作流节点列表查询.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiWorkflowNodeDTO implements Serializable {

    @Schema(description = "工作流定义ID", hidden = true)
    @NotNull(message = "工作流定义ID不能为空")
    private Long workflowDefinitionId;

    @Schema(description = "节点Key")
    private String nodeKey;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;
}
