package io.github.module.adminapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class WebClientConfigurationTest {

    @Test
    void webClientBuilderCreatesBean() {
        WebClient.Builder builder = new WebClientConfiguration().webClientBuilder();

        assertThat(builder).isNotNull();
    }
}
