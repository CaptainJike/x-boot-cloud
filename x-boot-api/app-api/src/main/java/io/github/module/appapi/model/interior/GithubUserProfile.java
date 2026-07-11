package io.github.module.appapi.model.interior;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * GitHub 用户信息.
 */
@Data
public class GithubUserProfile {

    private Long id;

    private String login;

    private String name;

    private String email;

    @JsonProperty("avatar_url")
    private String avatarUrl;
}
