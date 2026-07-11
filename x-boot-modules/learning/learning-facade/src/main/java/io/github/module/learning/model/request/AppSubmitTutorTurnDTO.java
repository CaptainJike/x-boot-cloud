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
 * Tutor 会话轮次提交请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Tutor 会话轮次提交请求")
public class AppSubmitTutorTurnDTO implements Serializable {

    @Schema(description = "用户回答", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "用户回答不能为空")
    @Size(max = 4000, message = "【用户回答】最长4000位")
    private String learnerAnswer;
}
