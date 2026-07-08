package io.github.module.adminapi.web.sys;

import cn.hutool.extra.spring.SpringUtil;
import io.github.framework.core.enums.GenderEnum;
import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.event.KickOutSysUsersEvent;
import io.github.module.adminapi.util.AdminStpUtil;
import io.github.module.sys.enums.SysUserStatusEnum;
import io.github.module.sys.facade.SysUserFacade;
import io.github.module.sys.model.request.AdminBindUserRoleRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysUserDTO;
import io.github.module.sys.model.request.AdminResetSysUserPasswordDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminSysUserControllerTest {

    @Mock
    private SysUserFacade sysUserFacade;

    private AdminSysUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSysUserController();
        ReflectionTestUtils.setField(controller, "sysUserFacade", sysUserFacade);
    }

    @Test
    void updateBannedUserPublishesKickOutEventAfterFacadeUpdate() {
        AdminInsertOrUpdateSysUserDTO dto = validUserDto(SysUserStatusEnum.BANNED);

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ApiResult<Void> result = controller.update(5L, dto);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(dto.getId()).isEqualTo(5L);
            assertThat(dto.getTenantId()).isNull();
            verify(sysUserFacade).adminUpdate(dto);
            KickOutSysUsersEvent event = capturePublishedKickOutEvent(springUtil);
            assertThat(event.getData().getSysUserIds()).containsExactly(5L);
        }
    }

    @Test
    void deletePublishesKickOutEventForDeletedUsers() {
        List<Long> userIds = List.of(6L, 7L);
        IdsDTO<Long> dto = new IdsDTO<Long>().setIds(userIds);

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ApiResult<Void> result = controller.delete(dto);

            assertThat(result.getCode()).isEqualTo(200);
            verify(sysUserFacade).adminDelete(userIds);
            KickOutSysUsersEvent event = capturePublishedKickOutEvent(springUtil);
            assertThat(event.getData().getSysUserIds()).containsExactlyElementsOf(userIds);
        }
    }

    @Test
    void resetPasswordKicksOutUserAfterFacadeUpdate() {
        AdminResetSysUserPasswordDTO dto = AdminResetSysUserPasswordDTO.builder()
                .randomPassword("random-password-123456")
                .build();

        try (MockedStatic<AdminStpUtil> adminStpUtil = mockStatic(AdminStpUtil.class)) {
            ApiResult<Void> result = controller.resetPassword(8L, dto);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(dto.getUserId()).isEqualTo(8L);
            verify(sysUserFacade).adminResetUserPassword(dto);
            adminStpUtil.verify(() -> AdminStpUtil.kickout(8L));
        }
    }

    @Test
    void bindRolesPublishesKickOutEventForChangedUserRoles() {
        AdminBindUserRoleRelationDTO dto = AdminBindUserRoleRelationDTO.builder()
                .roleIds(Set.of(1L, 2L))
                .build();

        try (MockedStatic<SpringUtil> springUtil = mockStatic(SpringUtil.class)) {
            ApiResult<Void> result = controller.bindRoles(9L, dto);

            assertThat(result.getCode()).isEqualTo(200);
            assertThat(dto.getUserId()).isEqualTo(9L);
            verify(sysUserFacade).adminBindRoles(dto);
            KickOutSysUsersEvent event = capturePublishedKickOutEvent(springUtil);
            assertThat(event.getData().getSysUserIds()).containsExactly(9L);
        }
    }

    private static KickOutSysUsersEvent capturePublishedKickOutEvent(MockedStatic<SpringUtil> springUtil) {
        ArgumentCaptor<ApplicationEvent> eventCaptor = ArgumentCaptor.forClass(ApplicationEvent.class);
        springUtil.verify(() -> SpringUtil.publishEvent(eventCaptor.capture()));
        assertThat(eventCaptor.getValue()).isInstanceOf(KickOutSysUsersEvent.class);
        return (KickOutSysUsersEvent) eventCaptor.getValue();
    }

    private static AdminInsertOrUpdateSysUserDTO validUserDto(SysUserStatusEnum status) {
        return AdminInsertOrUpdateSysUserDTO.builder()
                .username("testuser")
                .nickname("测试用户")
                .status(status)
                .gender(GenderEnum.UNKNOWN)
                .email("test@example.com")
                .phoneNo("13800138000")
                .build();
    }
}
