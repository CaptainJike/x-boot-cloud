package io.github.starter.ai.service;

import io.github.starter.ai.vo.AiChatRequest;
import io.github.starter.ai.vo.AiModelConfig;
import reactor.core.publisher.Flux;

public interface XBootAiService {

    /**
     * 生成响应.
     *
     * @param message 用户问题
     * @param modelConfig 模型配置
     * @return 响应内容
     */
    String chat(String message, AiModelConfig modelConfig);

    /**
     * 生成多模态响应.
     *
     * @param request 对话请求
     * @param modelConfig 模型配置
     * @return 响应内容
     */
    String chat(AiChatRequest request, AiModelConfig modelConfig);

    /**
     * 流式生成响应.
     *
     * @param message 用户问题
     * @param modelConfig 模型配置
     * @return 响应内容
     */
    Flux<String> stream(String message, AiModelConfig modelConfig);

    /**
     * 流式生成多模态响应.
     *
     * @param request 对话请求
     * @param modelConfig 模型配置
     * @return 响应内容
     */
    Flux<String> stream(AiChatRequest request, AiModelConfig modelConfig);

}
