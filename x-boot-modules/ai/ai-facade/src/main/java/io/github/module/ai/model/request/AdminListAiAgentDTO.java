package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-Agent 分页查询.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiAgentDTO implements Serializable {

    @Schema(description = "Agent编码")
    private String agentCode;

    @Schema(description = "Agent名称")
    private String name;

    @Schema(description = "默认模型配置编码")
    private String modelConfigCode;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;

    @Schema(description = "发布状态(0=草稿 1=已发布)")
    private Integer publishStatus;
}
