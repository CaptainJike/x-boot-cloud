package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 保存复盘记录请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "保存复盘记录请求")
public class AppSaveReviewAttemptDTO implements Serializable {

    @Schema(description = "学习目标ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "学习目标ID不能为空")
    private Long goalId;

    @Schema(description = "客户端变更ID，用于幂等重试", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "客户端变更ID不能为空")
    @Size(max = 64, message = "【客户端变更ID】最长64位")
    private String mutationId;

    @Schema(description = "提交基于的服务端版本，新记录为0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "基础版本不能为空")
    @Min(value = 0, message = "基础版本不能小于0")
    private Long baseVersion;

    @Schema(description = "复盘回答")
    @Size(max = 16000, message = "【复盘回答】最长16000位")
    private String response;

    @Schema(description = "学习者自评", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学习者自评不能为空")
    @Pattern(regexp = "solid|wobbly|forgotten", message = "学习者自评无效")
    private String selfRating;

    @Schema(description = "是否完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "完成状态不能为空")
    private Boolean completed;

    @Schema(description = "本次任务原定到期时间")
    @Size(max = 40, message = "【原定到期时间】最长40位")
    private String scheduledDueAt;

    @Schema(description = "建议间隔天数")
    @Min(value = 0, message = "间隔天数不能小于0")
    private Integer intervalDays;

    @Schema(description = "提交时的掌握度快照")
    @Min(value = 0, message = "掌握度不能小于0")
    private Integer masteryScoreAtAttempt;

    @Schema(description = "客户端更新时间")
    @Size(max = 40, message = "【客户端更新时间】最长40位")
    private String clientUpdatedAt;
}
