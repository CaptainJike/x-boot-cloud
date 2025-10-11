package io.github.framework.core.exception;

/**
 * 限流策略异常
 * 用于x-boot-starter-rate-limit-redis
 */
public class RateLimitStrategyException extends XBootFrameworkException {

    public RateLimitStrategyException(String msg) {
        super(msg);
    }
}
