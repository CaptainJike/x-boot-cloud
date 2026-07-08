package io.github.module.ai.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 后台管理-执行知识库检索.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminRetrieveAiKnowledgeDTO implements Serializable {

    @Schema(description = "知识库ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 20, message = "【知识库ID列表】最多20个")
    @NotEmpty(message = "知识库ID不能为空")
    private List<Long> knowledgeBaseIds;

    @Schema(description = "查询内容", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 4000, message = "【查询内容】最长4000位")
    @NotBlank(message = "查询内容不能为空")
    private String query;

    @Schema(description = "召回数量")
    @Min(value = 1, message = "【召回数量】不能小于1")
    @Max(value = 50, message = "【召回数量】不能大于50")
    private Integer topK;

    @Schema(description = "相似度阈值")
    @DecimalMin(value = "0.0", message = "【相似度阈值】不能小于0")
    @DecimalMax(value = "1.0", message = "【相似度阈值】不能大于1")
    private Double similarityThreshold;

    @Schema(description = "业务会话ID")
    @Size(max = 64, message = "【业务会话ID】最长64位")
    private String conversationId;

    @Schema(description = "业务消息ID")
    @Size(max = 64, message = "【业务消息ID】最长64位")
    private String messageId;

    @Schema(description = "是否记录检索日志")
    private Boolean logFlag;
}
