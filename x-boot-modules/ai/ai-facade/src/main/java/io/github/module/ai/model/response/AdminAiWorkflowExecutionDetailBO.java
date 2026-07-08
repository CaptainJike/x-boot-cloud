package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * AI 工作流执行记录详情 BO.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiWorkflowExecutionDetailBO extends AdminAiWorkflowExecutionBO {

    @Schema(description = "输入摘要JSON")
    private String inputSummary;

    @Schema(description = "输出摘要JSON")
    private String outputSummary;
}
