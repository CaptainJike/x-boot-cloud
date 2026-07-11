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
 * APP 侧 GitHub 登录请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "APP 侧 GitHub 登录请求")
public class AppGithubLoginDTO implements Serializable {

    @Schema(description = "GitHub 用户唯一ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "GitHub 用户ID不能为空")
    @Size(max = 64, message = "【GitHub 用户ID】最长64位")
    private String githubUserId;

    @Schema(description = "GitHub 登录名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "GitHub 登录名不能为空")
    @Size(max = 255, message = "【GitHub 登录名】最长255位")
    private String githubLogin;

    @Schema(description = "GitHub 昵称")
    @Size(max = 255, message = "【GitHub 昵称】最长255位")
    private String githubName;

    @Schema(description = "GitHub 头像")
    @Size(max = 500, message = "【GitHub 头像】最长500位")
    private String githubAvatarUrl;

    @Schema(description = "GitHub 邮箱")
    @Size(max = 255, message = "【GitHub 邮箱】最长255位")
    private String githubEmail;
}
