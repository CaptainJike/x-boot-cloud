package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 后台 AI 实时核验引用来源.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AdminAiRealtimeReferenceBO implements Serializable {

    @Schema(description = "来源名称")
    private String sourceName;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "链接")
    private String url;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "发布时间")
    private String publishedAt;
}
