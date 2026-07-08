package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 工作流 HTTP 工具请求.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowHttpToolRequest {

    private String method;

    private String url;

    private Map<String, String> headers;

    private String body;

    private Integer timeoutMs;
}
