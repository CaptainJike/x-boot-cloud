package io.github.module.adminapi.service;

import cn.hutool.core.util.StrUtil;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.exception.BusinessException;
import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.request.AdminAiChatStreamRequestDTO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 后台 AI 流式对话代理客户端，使用 HTTP SSE 避免 Dubbo 阻塞完整响应.
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class AdminAiChatStreamClient {

    private static final String DEFAULT_AI_STREAM_SERVICE_ID = "ai-service-rest";
    private static final String INTERNAL_STREAM_PATH = "/internal-api/v1/ai/chats/stream";
    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";
    private static final String EVENT_ERROR = "error";

    private static final ParameterizedTypeReference<ServerSentEvent<AdminAiChatStreamChunkBO>> STREAM_EVENT_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final DiscoveryClient discoveryClient;

    private final WebClient.Builder webClientBuilder;

    @Value("${x.ai.stream.service-id:" + DEFAULT_AI_STREAM_SERVICE_ID + "}")
    private String serviceId;

    @Value("${x.ai.stream.base-url:}")
    private String baseUrl;

    @Value("${x.ai.stream.internal-token:}")
    private String internalToken;

    public Flux<AdminAiChatStreamChunkBO> stream(AdminAiChatDTO dto) {
        AdminAiChatStreamRequestDTO request = AdminAiChatStreamRequestDTO.builder()
                .chat(dto)
                .userContext(UserContextHolder.getUserContext())
                .tenantContext(TenantContextHolder.getTenantContext())
                .build();

        return Flux.defer(() -> webClientBuilder.clone()
                        .baseUrl(resolveBaseUrl())
                        .build()
                        .post()
                        .uri(INTERNAL_STREAM_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .headers(headers -> {
                            if (StrUtil.isNotBlank(internalToken)) {
                                headers.set(INTERNAL_TOKEN_HEADER, internalToken);
                            }
                        })
                        .bodyValue(request)
                        .retrieve()
                        .bodyToFlux(STREAM_EVENT_TYPE)
                        .map(ServerSentEvent::data)
                        .filter(Objects::nonNull))
                .onErrorResume(ex -> {
                    log.warn("[AI][Stream] 后台流式代理调用失败", ex);
                    return Flux.just(errorChunk(dto, ex));
                });
    }

    private String resolveBaseUrl() {
        if (StrUtil.isNotBlank(baseUrl)) {
            return trimTrailingSlash(baseUrl);
        }

        String cleanServiceId = StrUtil.blankToDefault(serviceId, DEFAULT_AI_STREAM_SERVICE_ID);
        List<ServiceInstance> instances = discoveryClient.getInstances(cleanServiceId);
        if (instances == null || instances.isEmpty()) {
            throw new BusinessException(503, "AI流式服务不可用，请检查服务发现：" + cleanServiceId);
        }

        ServiceInstance instance = instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
        URI uri = instance.getUri();
        if (uri == null) {
            throw new BusinessException(503, "AI流式服务地址无效：" + cleanServiceId);
        }
        return trimTrailingSlash(uri.toString());
    }

    private String trimTrailingSlash(String url) {
        return StrUtil.removeSuffix(StrUtil.trim(url), "/");
    }

    private AdminAiChatStreamChunkBO errorChunk(AdminAiChatDTO dto, Throwable ex) {
        return AdminAiChatStreamChunkBO.builder()
                .event(EVENT_ERROR)
                .conversationId(dto == null ? null : dto.getConversationId())
                .content(rootMessage(ex))
                .finish(true)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return StrUtil.blankToDefault(root.getMessage(), "AI流式对话调用失败");
    }
}
