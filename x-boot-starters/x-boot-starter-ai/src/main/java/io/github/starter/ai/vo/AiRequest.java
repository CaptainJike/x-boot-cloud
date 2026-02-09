package io.github.starter.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "ai请求类")
public class AiRequest implements Serializable {

    @Schema(description = "对话内容")
    private String message;

    @Schema(description = "模型选择")
    private String modelType;
}
