package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 每日反思提交请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "每日反思提交请求")
public class AppSubmitDailyReflectionDTO implements Serializable {

    @Schema(description = "今天学到了什么", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "今天学到了什么不能为空")
    @Size(max = 4000, message = "【今天学到了什么】最长4000位")
    private String learnedToday;

    @Schema(description = "今天最大的收获", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "今天最大的收获不能为空")
    @Size(max = 4000, message = "【今天最大的收获】最长4000位")
    private String biggestInsight;

    @Schema(description = "今天新的认知")
    @Size(max = 4000, message = "【今天新的认知】最长4000位")
    private String newAwareness;

    @Schema(description = "今天哪里不会", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "今天哪里不会不能为空")
    @Size(max = 4000, message = "【今天哪里不会】最长4000位")
    private String unresolvedQuestion;

    @Schema(description = "为什么不会", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "为什么不会不能为空")
    @Size(max = 4000, message = "【为什么不会】最长4000位")
    private String whyStuck;
}
