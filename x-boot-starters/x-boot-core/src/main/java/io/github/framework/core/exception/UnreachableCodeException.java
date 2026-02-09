package io.github.framework.core.exception;

/**
 * 不可达代码异常类
 */
public class UnreachableCodeException extends RuntimeException {

    public UnreachableCodeException() {
        super("Execution path unreachable");
    }
}
