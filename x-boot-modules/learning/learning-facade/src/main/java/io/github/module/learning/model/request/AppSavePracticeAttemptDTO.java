package io.github.module.learning.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
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
import java.util.List;

/**
 * 保存练习记录请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "保存练习记录请求")
public class AppSavePracticeAttemptDTO implements Serializable {

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

    @Schema(description = "练习回答")
    @Size(max = 16000, message = "【练习回答】最长16000位")
    private String response;

    @Schema(description = "学习者自评", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "学习者自评不能为空")
    @Pattern(regexp = "clear|stretch|stuck", message = "学习者自评无效")
    private String selfRating;

    @Schema(description = "证据列表")
    @Valid
    @Size(max = 10, message = "单次练习最多提交10条证据")
    private List<ArtifactDTO> artifacts;

    @Schema(description = "规则评测结果")
    @Valid
    private AssessmentDTO assessment;

    @Schema(description = "是否完成", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "完成状态不能为空")
    private Boolean completed;

    @Schema(description = "是否为目标交接验证")
    private Boolean handoffValidation;

    @Schema(description = "客户端更新时间")
    @Size(max = 40, message = "【客户端更新时间】最长40位")
    private String clientUpdatedAt;

    /**
     * 可复核证据.
     */
    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ArtifactDTO implements Serializable {

        @NotBlank(message = "证据ID不能为空")
        @Size(max = 160, message = "【证据ID】最长160位")
        private String id;

        @NotBlank(message = "证据类型不能为空")
        @Pattern(regexp = "work|code|link|file", message = "证据类型无效")
        private String kind;

        @NotBlank(message = "证据标题不能为空")
        @Size(max = 255, message = "【证据标题】最长255位")
        private String title;

        @Size(max = 16000, message = "【证据内容】最长16000位")
        private String content;

        @Size(max = 1000, message = "【证据链接】最长1000位")
        private String url;

        @Size(max = 64, message = "【代码语言】最长64位")
        private String language;

        @Size(max = 255, message = "【文件名】最长255位")
        private String fileName;

        @Size(max = 120, message = "【文件类型】最长120位")
        private String mimeType;

        @Min(value = 0, message = "文件大小不能小于0")
        private Long sizeBytes;

        @NotBlank(message = "证据创建时间不能为空")
        @Size(max = 40, message = "【证据创建时间】最长40位")
        private String createdAt;
    }

    /**
     * 规则评测结果.
     */
    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class AssessmentDTO implements Serializable {

        @NotBlank(message = "评测模式不能为空")
        @Pattern(regexp = "rule", message = "评测模式无效")
        private String mode;

        @NotBlank(message = "评测等级不能为空")
        @Pattern(regexp = "verified|partial|needs_work", message = "评测等级无效")
        private String level;

        @NotNull(message = "评测分数不能为空")
        @Min(value = 0, message = "评测分数不能小于0")
        @Max(value = 100, message = "评测分数不能大于100")
        private Integer score;

        @NotBlank(message = "评测摘要不能为空")
        @Size(max = 1000, message = "【评测摘要】最长1000位")
        private String summary;

        @Valid
        @Size(max = 10, message = "评测标准最多10条")
        private List<CriterionDTO> criteria;

        @NotBlank(message = "评测时间不能为空")
        @Size(max = 40, message = "【评测时间】最长40位")
        private String assessedAt;
    }

    /**
     * 单项评测标准.
     */
    @Accessors(chain = true)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class CriterionDTO implements Serializable {

        @NotBlank(message = "评测标准ID不能为空")
        @Size(max = 64, message = "【评测标准ID】最长64位")
        private String id;

        @NotBlank(message = "评测标准名称不能为空")
        @Size(max = 120, message = "【评测标准名称】最长120位")
        private String label;

        @NotBlank(message = "评测标准状态不能为空")
        @Pattern(regexp = "met|partial|missing", message = "评测标准状态无效")
        private String status;

        @NotNull(message = "评测标准得分不能为空")
        @Min(value = 0, message = "评测标准得分不能小于0")
        private Integer score;

        @NotNull(message = "评测标准满分不能为空")
        @Min(value = 1, message = "评测标准满分不能小于1")
        private Integer maxScore;

        @NotBlank(message = "评测反馈不能为空")
        @Size(max = 1000, message = "【评测反馈】最长1000位")
        private String feedback;
    }
}
