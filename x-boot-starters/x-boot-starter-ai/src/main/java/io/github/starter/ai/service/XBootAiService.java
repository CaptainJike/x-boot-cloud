package io.github.starter.ai.service;

import reactor.core.publisher.Flux;

public interface XBootAiService {

    /**
     * 生成响应。
     * @param message 用户问题
     * @return 响应内容
     */
    String chat(String message);

    /**
     * 流式生成响应。
     * @param message 用户问题
     * @return 响应内容
     */
    Flux<String> stream(String message);

}
