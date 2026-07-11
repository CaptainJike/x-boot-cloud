package io.github.module.learning.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.framework.crud.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * C 端学习者账号.
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("learner_account")
public class LearnerAccountEntity extends BaseEntity<Long> {

    @Schema(description = "学习者编号")
    @TableField("learner_no")
    private String learnerNo;

    @Schema(description = "昵称")
    @TableField("nickname")
    private String nickname;

    @Schema(description = "状态")
    @TableField("status")
    private EnabledStatusEnum status;

    @Schema(description = "邮箱")
    @TableField("email")
    private String email;

    @Schema(description = "手机号")
    @TableField("phone_no")
    private String phoneNo;

    @Schema(description = "最后登录时刻")
    @TableField("last_login_at")
    private LocalDateTime lastLoginAt;

    @Schema(description = "头像URL")
    @TableField("avatar_url")
    private String avatarUrl;

    @Schema(description = "GitHub 用户ID")
    @TableField("github_user_id")
    private String githubUserId;

    @Schema(description = "GitHub 登录名")
    @TableField("github_login")
    private String githubLogin;
}
