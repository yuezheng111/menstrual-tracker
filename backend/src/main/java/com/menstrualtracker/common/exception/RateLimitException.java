package com.menstrualtracker.common.exception;

/**
 * 限流异常 — 当请求超过 Redis 限流阈值时抛出，由全局异常处理器返回 HTTP 429。
 */
public class RateLimitException extends RuntimeException {

    private final int code = 429;

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException() {
        super("Too many requests, please try again later");
    }

    public int getCode() {
        return code;
    }
}
