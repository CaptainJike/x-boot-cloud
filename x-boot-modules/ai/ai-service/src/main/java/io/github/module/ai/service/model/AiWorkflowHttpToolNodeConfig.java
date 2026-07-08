package io.github.module.ai.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 工作流 HTTP 工具节点配置.
 */
@Accessors(chain = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AiWorkflowHttpToolNodeConfig {

    private String method;

    private String urlTemplate;

    private List<String> allowedHosts;

    private Map<String, String> headers;

    private String authScheme;

    private String authTokenVariable;

    private String bodyTemplate;

    private String inputVariable;

    private String outputVariable;

    private List<Integer> successStatusCodes;

    private Integer timeoutMs;

    private Integer maxResponseLength;

    private List<String> sensitiveResponseKeys;
}
