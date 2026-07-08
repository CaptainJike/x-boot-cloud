package io.github.module.ai.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.framework.core.constant.BaseConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AI 工作流节点详情 BO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowNodeDetailBO implements Serializable {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "创建时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时刻")
    @DateTimeFormat(pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BaseConstant.Jackson.DATE_TIME_FORMAT)
    private LocalDateTime updatedAt;

    @Schema(description = "工作流定义ID")
    private Long workflowDefinitionId;

    @Schema(description = "工作流编码")
    private String workflowCode;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "节点Key")
    private String nodeKey;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "节点描述")
    private String description;

    @Schema(description = "节点配置JSON")
    private String nodeConfig;

    @Schema(description = "输入映射JSON")
    private String inputMapping;

    @Schema(description = "输出映射JSON")
    private String outputMapping;

    @Schema(description = "下游节点Key列表")
    private String nextNodeKeys;

    @Schema(description = "条件表达式")
    private String conditionExpression;

    @Schema(description = "错误策略")
    private String errorStrategy;

    @Schema(description = "重试次数")
    private Integer retryCount;

    @Schema(description = "超时时间，单位秒")
    private Long timeoutSeconds;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态(0=禁用 1=启用)")
    private Integer status;
}
