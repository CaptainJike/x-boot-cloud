package io.github.module.ai.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * AI 供应商模型.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiProviderModelBO implements Serializable {

    @Schema(description = "模型ID")
    private String id;

    @Schema(description = "模型名称")
    private String name;
}
