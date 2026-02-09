package io.github.starter.ai.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "ai返回类")
public class AiResponse implements Serializable {

    @Schema(description = "内容")
    private String answer;

    @Schema(description = "模型")
    private String modelUsed;
}
