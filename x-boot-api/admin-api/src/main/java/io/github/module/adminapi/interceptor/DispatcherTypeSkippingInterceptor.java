package io.github.module.adminapi.interceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 跳过 Servlet 异步/错误二次派发，避免没有 Sa-Token ThreadLocal 上下文时重复鉴权.
 */
@RequiredArgsConstructor
public class DispatcherTypeSkippingInterceptor implements AsyncHandlerInterceptor {

    private final HandlerInterceptor delegate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (shouldSkip(request)) {
            return true;
        }
        return delegate.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
        if (!shouldSkip(request)) {
            delegate.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                @Nullable Exception ex) throws Exception {
        if (!shouldSkip(request)) {
            delegate.afterCompletion(request, response, handler, ex);
        }
    }

    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Object handler) throws Exception {
        if (!shouldSkip(request) && delegate instanceof AsyncHandlerInterceptor asyncHandlerInterceptor) {
            asyncHandlerInterceptor.afterConcurrentHandlingStarted(request, response, handler);
        }
    }

    static boolean shouldSkip(HttpServletRequest request) {
        DispatcherType dispatcherType = request.getDispatcherType();
        return dispatcherType == DispatcherType.ASYNC || dispatcherType == DispatcherType.ERROR;
    }
}
