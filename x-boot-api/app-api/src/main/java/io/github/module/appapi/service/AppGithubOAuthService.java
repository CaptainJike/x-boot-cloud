package io.github.module.appapi.service;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import io.github.framework.core.exception.BusinessException;
import io.github.module.appapi.config.AppGithubOAuthProperties;
import io.github.module.appapi.model.interior.GithubAccessTokenResponse;
import io.github.module.appapi.model.interior.GithubUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * GitHub OAuth 服务.
 */
@RequiredArgsConstructor
@Service
public class AppGithubOAuthService {

    private static final String STATE_CACHE_KEY = "LearningOS:github:state:%s";

    private final AppGithubOAuthProperties properties;

    private final WebClient.Builder webClientBuilder;

    private final RedisTemplate<String, Object> redisTemplate;

    public String buildAuthorizeUrl() {
        validateConfig();
        String state = IdUtil.fastSimpleUUID();
        redisTemplate.opsForValue().set(cacheKey(state), "1", properties.getStateTtlSeconds(), TimeUnit.SECONDS);
        return properties.getAuthorizeUrl()
                + "?client_id=" + urlEncode(properties.getClientId())
                + "&redirect_uri=" + urlEncode(properties.getRedirectUri())
                + "&scope=" + urlEncode("read:user user:email")
                + "&state=" + urlEncode(state);
    }

    public GithubUserProfile exchangeCodeForUser(String code, String state) {
        validateConfig();
        validateState(state);

        GithubAccessTokenResponse tokenResponse = webClientBuilder.build()
                .post()
                .uri(properties.getAccessTokenUrl())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", properties.getClientId())
                        .with("client_secret", properties.getClientSecret())
                        .with("code", code)
                        .with("redirect_uri", properties.getRedirectUri())
                        .with("state", state))
                .retrieve()
                .bodyToMono(GithubAccessTokenResponse.class)
                .block();

        if (tokenResponse == null || CharSequenceUtil.isBlank(tokenResponse.getAccessToken())) {
            throw new BusinessException(400, "GitHub access token 获取失败");
        }

        return webClientBuilder.build()
                .get()
                .uri(properties.getUserApiUrl())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenResponse.getAccessToken())
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(GithubUserProfile.class)
                .block();
    }

    private void validateState(String state) {
        if (CharSequenceUtil.isBlank(state)) {
            throw new BusinessException(400, "GitHub state 不能为空");
        }
        String key = cacheKey(state);
        Object cached = redisTemplate.opsForValue().get(key);
        redisTemplate.delete(key);
        if (cached == null) {
            throw new BusinessException(400, "GitHub state 无效或已过期");
        }
    }

    private void validateConfig() {
        if (CharSequenceUtil.hasBlank(properties.getClientId(), properties.getClientSecret(), properties.getRedirectUri())) {
            throw new BusinessException(500, "GitHub OAuth 配置不完整");
        }
    }

    private String cacheKey(String state) {
        return String.format(STATE_CACHE_KEY, state);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(CharSequenceUtil.blankToDefault(value, ""), StandardCharsets.UTF_8);
    }
}
