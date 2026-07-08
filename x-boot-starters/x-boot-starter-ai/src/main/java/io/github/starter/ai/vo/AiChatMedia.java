package io.github.starter.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 对话媒体内容.
 */
@Data
@Accessors(chain = true)
@Schema(description = "AI对话媒体内容")
public class AiChatMedia implements Serializable {

    @Schema(description = "MIME类型")
    private String mimeType;

    @Schema(description = "媒体名称")
    private String name;

    @Schema(description = "媒体字节")
    private byte[] data;

    @Schema(description = "媒体URI")
    private String uri;
}
