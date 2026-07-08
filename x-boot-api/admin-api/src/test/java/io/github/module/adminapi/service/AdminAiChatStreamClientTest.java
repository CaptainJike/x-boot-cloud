package io.github.module.adminapi.service;

import io.github.module.ai.model.request.AdminAiChatDTO;
import io.github.module.ai.model.response.AdminAiChatStreamChunkBO;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdminAiChatStreamClientTest {

    @Test
    void streamPostsToInternalSseEndpointAndDecodesEvents() {
        AtomicReference<ClientRequest> requestRef = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            requestRef.set(request);
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                    .body("""
                            id: msg-1
                            event: message
                            data: {"event":"message","messageId":"msg-1","conversationId":"conv-1","content":"hello","finish":false}

                            """)
                    .build());
        };
        AdminAiChatStreamClient client = new AdminAiChatStreamClient(
                mock(DiscoveryClient.class), WebClient.builder().exchangeFunction(exchangeFunction));
        ReflectionTestUtils.setField(client, "baseUrl", "http://ai-service/");
        ReflectionTestUtils.setField(client, "internalToken", "secret");

        List<AdminAiChatStreamChunkBO> chunks = client.stream(AdminAiChatDTO.builder()
                        .conversationId("conv-1")
                        .content("hi")
                        .build())
                .collectList()
                .block();

        assertThat(chunks).hasSize(1);
        assertThat(chunks.getFirst().getEvent()).isEqualTo("message");
        assertThat(chunks.getFirst().getMessageId()).isEqualTo("msg-1");
        assertThat(chunks.getFirst().getContent()).isEqualTo("hello");
        assertThat(requestRef.get().method()).isEqualTo(HttpMethod.POST);
        assertThat(requestRef.get().url().toString()).isEqualTo("http://ai-service/internal-api/v1/ai/chats/stream");
        assertThat(requestRef.get().headers().getFirst("X-Internal-Token")).isEqualTo("secret");
    }
}
