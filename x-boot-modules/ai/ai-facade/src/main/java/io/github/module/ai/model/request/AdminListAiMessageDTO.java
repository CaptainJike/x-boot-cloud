package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台管理-分页列表 AI 消息.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminListAiMessageDTO implements Serializable {

    @Schema(description = "消息角色(user/assistant/system)")
    private String role;

    @Schema(description = "状态(0=失败 1=成功 2=生成中)")
    private Integer status;
}
