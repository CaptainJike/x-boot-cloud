package io.github.module.ai.service.workflow;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import io.github.module.ai.service.model.AiWorkflowHttpToolRequest;
import io.github.module.ai.service.model.AiWorkflowHttpToolResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 基于 Hutool 的工作流 HTTP 工具调用适配器.
 */
@Component
public class HutoolAiWorkflowHttpToolClient implements AiWorkflowHttpToolClient {

    @Override
    public AiWorkflowHttpToolResponse exchange(AiWorkflowHttpToolRequest request) {
        HttpRequest httpRequest = HttpRequest.of(request.getUrl())
                .method(Method.valueOf(request.getMethod()))
                .timeout(request.getTimeoutMs());
        for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
            httpRequest.header(entry.getKey(), entry.getValue(), true);
        }
        if (request.getBody() != null) {
            httpRequest.body(request.getBody());
        }
        try (HttpResponse response = httpRequest.execute()) {
            return AiWorkflowHttpToolResponse.builder()
                    .statusCode(response.getStatus())
                    .body(response.body())
                    .build();
        }
    }
}
