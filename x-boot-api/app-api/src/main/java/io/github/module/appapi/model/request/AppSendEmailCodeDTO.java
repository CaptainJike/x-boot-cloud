package io.github.module.appapi.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP 侧发送邮箱验证码请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "APP 侧发送邮箱验证码请求")
public class AppSendEmailCodeDTO implements Serializable {

    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 255, message = "【邮箱】最长255位")
    private String email;

    @Schema(description = "业务场景", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "验证码场景不能为空")
    private AppEmailCodeScene scene;
}
