package io.github.module.adminapi.listener;

import io.github.module.adminapi.event.KickOutSysUsersEvent;
import io.github.module.adminapi.util.AdminStpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;

import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AdminApiEventListenerTest {

    @Mock
    private ThreadPoolTaskExecutor taskExecutor;

    private AdminApiEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new AdminApiEventListener(taskExecutor);
    }

    @Test
    void handleKickOutSysUsersEventSubmitsTaskThatKicksOutEveryUser() {
        KickOutSysUsersEvent event = new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(List.of(11L, 12L))
        );
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);

        listener.handleKickOutSysUsersEvent(event);

        verify(taskExecutor).submit(runnableCaptor.capture());
        try (MockedStatic<AdminStpUtil> adminStpUtil = mockStatic(AdminStpUtil.class)) {
            runnableCaptor.getValue().run();

            adminStpUtil.verify(() -> AdminStpUtil.kickout(11L));
            adminStpUtil.verify(() -> AdminStpUtil.kickout(12L));
        }
    }

    @Test
    void handleKickOutSysUsersEventIgnoresEmptyUserIds() {
        KickOutSysUsersEvent event = new KickOutSysUsersEvent(
                new KickOutSysUsersEvent.EventData(List.of())
        );

        listener.handleKickOutSysUsersEvent(event);

        verifyNoInteractions(taskExecutor);
    }
}
