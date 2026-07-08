package io.github.module.adminapi.interceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DispatcherTypeSkippingInterceptorTest {

    @Test
    void preHandleSkipsAsyncAndErrorDispatches() throws Exception {
        HandlerInterceptor delegate = mock(HandlerInterceptor.class);
        DispatcherTypeSkippingInterceptor interceptor = new DispatcherTypeSkippingInterceptor(delegate);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();

        when(request.getDispatcherType()).thenReturn(DispatcherType.ASYNC, DispatcherType.ERROR);

        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
        assertThat(interceptor.preHandle(request, response, handler)).isTrue();
        verify(delegate, never()).preHandle(request, response, handler);
    }

    @Test
    void preHandleDelegatesRequestDispatch() throws Exception {
        HandlerInterceptor delegate = mock(HandlerInterceptor.class);
        DispatcherTypeSkippingInterceptor interceptor = new DispatcherTypeSkippingInterceptor(delegate);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        Object handler = new Object();

        when(request.getDispatcherType()).thenReturn(DispatcherType.REQUEST);
        when(delegate.preHandle(request, response, handler)).thenReturn(false);

        assertThat(interceptor.preHandle(request, response, handler)).isFalse();
        verify(delegate).preHandle(request, response, handler);
    }
}
