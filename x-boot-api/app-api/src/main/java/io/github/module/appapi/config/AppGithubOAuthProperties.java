package io.github.module.appapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * GitHub OAuth 配置.
 */
@ConfigurationProperties(prefix = "x.learning.github")
@Data
public class AppGithubOAuthProperties {

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String authorizeUrl = "https://github.com/login/oauth/authorize";

    private String accessTokenUrl = "https://github.com/login/oauth/access_token";

    private String userApiUrl = "https://api.github.com/user";

    private Long stateTtlSeconds = 300L;
}
