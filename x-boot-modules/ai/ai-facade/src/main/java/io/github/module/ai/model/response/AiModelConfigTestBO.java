package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 模型配置检测结果.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiModelConfigTestBO implements Serializable {

    @Schema(description = "是否检测成功")
    private Boolean success;

    @Schema(description = "检测结果消息")
    private String message;

    @Schema(description = "耗时毫秒")
    private Long elapsedMilliseconds;

    @Schema(description = "配置编码")
    private String code;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "供应商类型")
    private String providerType;

    @Schema(description = "模型服务地址")
    private String baseUrl;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "API Key 脱敏值")
    private String apiKeyMasked;

    @Schema(description = "是否已配置 API Key")
    private Boolean apiKeyPresent;

    @Schema(description = "模型响应预览")
    private String answerPreview;
}
