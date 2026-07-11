package io.github.module.learning.model.response;

import io.github.framework.core.context.TenantContext;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * APP 侧学习者登录响应.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "APP 侧学习者登录响应")
public class AppLearnerLoginBO implements Serializable {

    @Schema(description = "学习者ID")
    private Long userId;

    @Schema(description = "学习者编号")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatarUrl;

    @Schema(description = "手机号")
    private String phoneNo;

    @Schema(description = "租户上下文")
    private TenantContext tenantContext;
}
