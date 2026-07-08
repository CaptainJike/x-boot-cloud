package io.github.starter.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 对话请求.
 */
@Data
@Accessors(chain = true)
@Schema(description = "AI对话请求")
public class AiChatRequest implements Serializable {

    @Schema(description = "文本内容")
    private String text;

    @Schema(description = "媒体内容")
    private List<AiChatMedia> media = new ArrayList<>();
}
