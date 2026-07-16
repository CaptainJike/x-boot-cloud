package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
 * Goal 调参建议 DTO.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Goal 调参建议")
public class AppGoalTuningSuggestionDTO implements Serializable {

    @Schema(description = "变更字段", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "变更字段不能为空")
    @Size(max = 64, message = "【变更字段】最长64位")
    private String field;

    @Schema(description = "建议标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "建议标题不能为空")
    @Size(max = 255, message = "【建议标题】最长255位")
    private String title;

    @Schema(description = "建议原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "建议原因不能为空")
    @Size(max = 2000, message = "【建议原因】最长2000位")
    private String rationale;

    @Schema(description = "修改前")
    @Size(max = 2000, message = "【修改前】最长2000位")
    private String before;

    @Schema(description = "修改后")
    @Size(max = 2000, message = "【修改后】最长2000位")
    private String after;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级不能为空")
    private Integer priority;
}
