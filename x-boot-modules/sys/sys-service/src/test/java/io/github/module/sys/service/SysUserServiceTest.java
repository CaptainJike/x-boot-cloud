package io.github.module.sys.service;

import cn.hutool.crypto.SecureUtil;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.GenderEnum;
import io.github.framework.core.props.BaseProperties;
import io.github.module.sys.constant.SysConstant;
import io.github.module.sys.entity.SysDeptEntity;
import io.github.module.sys.entity.SysRoleEntity;
import io.github.module.sys.entity.SysUserEntity;
import io.github.module.sys.enums.SysUserStatusEnum;
import io.github.module.sys.mapper.SysUserMapper;
import io.github.module.sys.model.interior.UserDeptContainer;
import io.github.module.sys.model.interior.UserRoleContainer;
import io.github.module.sys.model.request.AdminBindUserRoleRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysUserDTO;
import io.github.module.sys.model.request.AdminResetSysUserPasswordDTO;
import io.github.module.sys.util.PwdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserServiceTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long TARGET_USER_ID = 2L;
    private static final Long DEPT_ID = 10L;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private SysRoleService sysRoleService;

    @Mock
    private SysDeptService sysDeptService;

    @Mock
    private SysMenuService sysMenuService;

    @Mock
    private SysTenantService sysTenantService;

    @Mock
    private SysUserDeptRelationService sysUserDeptRelationService;

    @Mock
    private SysUserRoleRelationService sysUserRoleRelationService;

    @Mock
    private SysRoleMenuRelationService sysRoleMenuRelationService;

    @Mock
    private BaseProperties baseProperties;

    @InjectMocks
    private SysUserService sysUserService;

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(CURRENT_USER_ID)
                .setUserName("superadmin")
                .setUserTypeStr("ADMIN_USER"));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void adminInsertCreatesUserWithEncryptedPasswordAndBindsDept() {
        AdminInsertOrUpdateSysUserDTO dto = validDto()
                .setPasswordOfNewUser("password123")
                .setDeptId(DEPT_ID);
        when(sysUserMapper.getUserByPin("newuser")).thenReturn(null);
        when(sysDeptService.getCurrentUserDeptContainer(true)).thenReturn(deptContainer(DEPT_ID));
        when(sysUserMapper.insert(any(SysUserEntity.class))).thenAnswer(invocation -> {
            SysUserEntity entity = invocation.getArgument(0);
            entity.setId(TARGET_USER_ID);
            return 1;
        });

        Long userId = sysUserService.adminInsert(dto);

        ArgumentCaptor<SysUserEntity> entityCaptor = ArgumentCaptor.forClass(SysUserEntity.class);
        verify(sysUserMapper).insert(entityCaptor.capture());
        SysUserEntity inserted = entityCaptor.getValue();
        assertThat(userId).isEqualTo(TARGET_USER_ID);
        assertThat(dto.getId()).isNull();
        assertThat(inserted.getPin()).isEqualTo("newuser");
        assertThat(inserted.getPwd()).isNotEqualTo("password123");
        assertThat(inserted.getSalt()).isNotBlank();
        assertThat(inserted.getPwd())
                .isEqualTo(PwdUtil.encrypt(SecureUtil.md5(SecureUtil.md5("password123")), inserted.getSalt()));
        verify(sysUserDeptRelationService).cleanAndBind(TARGET_USER_ID, DEPT_ID);
    }

    @Test
    void adminUpdateUpdatesUserBaseInfoAndDeptRelation() {
        AdminInsertOrUpdateSysUserDTO dto = validDto()
                .setId(TARGET_USER_ID)
                .setUsername("edituser")
                .setNickname("编辑用户")
                .setDeptId(DEPT_ID);
        when(sysRoleService.getCurrentUserRoleContainer()).thenReturn(superAdminContainer());
        when(sysUserMapper.getUserByPin("edituser")).thenReturn(existingUser(TARGET_USER_ID, "edituser"));

        sysUserService.adminUpdate(dto);

        ArgumentCaptor<SysUserEntity> entityCaptor = ArgumentCaptor.forClass(SysUserEntity.class);
        verify(sysUserDeptRelationService).cleanAndBind(TARGET_USER_ID, DEPT_ID);
        verify(sysUserMapper).updateById(entityCaptor.capture());
        SysUserEntity updated = entityCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(TARGET_USER_ID);
        assertThat(updated.getPin()).isEqualTo("edituser");
        assertThat(updated.getNickname()).isEqualTo("编辑用户");
        assertThat(updated.getStatus()).isEqualTo(SysUserStatusEnum.ENABLED);
    }

    @Test
    void adminDeleteDeletesAllowedUsers() {
        when(sysRoleService.getSpecifiedUserRoleContainer(anyLong())).thenReturn(nonAdminContainer());
        when(sysRoleService.getCurrentUserRoleContainer()).thenReturn(superAdminContainer());
        when(sysDeptService.getCurrentUserDeptContainer(true)).thenReturn(emptyDeptContainer());
        when(sysRoleService.determineInvisibleRoleIds()).thenReturn(Collections.emptySet());
        when(sysUserRoleRelationService.listUserIdsByRoleIds(any())).thenReturn(Collections.emptySet());

        sysUserService.adminDelete(List.of(TARGET_USER_ID, 3L));

        verify(sysUserMapper).deleteBatchIds(List.of(TARGET_USER_ID, 3L));
    }

    @Test
    void adminResetUserPasswordStoresEncryptedPassword() {
        AdminResetSysUserPasswordDTO dto = AdminResetSysUserPasswordDTO.builder()
                .userId(TARGET_USER_ID)
                .randomPassword("randomPassword123456")
                .build();
        when(sysRoleService.getCurrentUserRoleContainer()).thenReturn(superAdminContainer());
        when(sysUserMapper.selectById(TARGET_USER_ID)).thenReturn(existingUser(TARGET_USER_ID, "target").setSalt("salt"));

        sysUserService.adminResetUserPassword(dto);

        ArgumentCaptor<SysUserEntity> entityCaptor = ArgumentCaptor.forClass(SysUserEntity.class);
        verify(sysUserMapper).updateById(entityCaptor.capture());
        SysUserEntity updated = entityCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(TARGET_USER_ID);
        assertThat(updated.getPwd()).isEqualTo(PwdUtil.encrypt("randomPassword123456", "salt"));
    }

    @Test
    void adminUpdateCanBanOtherUser() {
        AdminInsertOrUpdateSysUserDTO dto = validDto()
                .setId(TARGET_USER_ID)
                .setStatus(SysUserStatusEnum.BANNED);
        when(sysRoleService.getCurrentUserRoleContainer()).thenReturn(superAdminContainer());
        when(sysUserMapper.getUserByPin("newuser")).thenReturn(existingUser(TARGET_USER_ID, "newuser"));

        sysUserService.adminUpdate(dto);

        ArgumentCaptor<SysUserEntity> entityCaptor = ArgumentCaptor.forClass(SysUserEntity.class);
        verify(sysUserMapper).updateById(entityCaptor.capture());
        assertThat(entityCaptor.getValue().getId()).isEqualTo(TARGET_USER_ID);
        assertThat(entityCaptor.getValue().getStatus()).isEqualTo(SysUserStatusEnum.BANNED);
    }

    @Test
    void adminBindRolesBindsAllowedRoleIds() {
        AdminBindUserRoleRelationDTO dto = AdminBindUserRoleRelationDTO.builder()
                .userId(TARGET_USER_ID)
                .roleIds(Set.of(2L, 3L))
                .build();
        when(sysRoleService.getCurrentUserRoleContainer()).thenReturn(superAdminContainer());

        sysUserService.adminBindRoles(dto);

        verify(sysUserRoleRelationService).cleanAndBind(TARGET_USER_ID, Set.of(2L, 3L));
    }

    private AdminInsertOrUpdateSysUserDTO validDto() {
        return AdminInsertOrUpdateSysUserDTO.builder()
                .username("newuser")
                .passwordOfNewUser("password123")
                .nickname("测试用户")
                .status(SysUserStatusEnum.ENABLED)
                .gender(GenderEnum.UNKNOWN)
                .email("test@example.com")
                .phoneNo("13800138000")
                .build();
    }

    private SysUserEntity existingUser(Long userId, String pin) {
        SysUserEntity entity = new SysUserEntity()
                .setPin(pin)
                .setNickname("目标用户")
                .setStatus(SysUserStatusEnum.ENABLED)
                .setGender(GenderEnum.UNKNOWN)
                .setEmail("target@example.com")
                .setPhoneNo("13800138001")
                .setSalt("salt");
        entity.setId(userId);
        return entity;
    }

    private UserRoleContainer superAdminContainer() {
        SysRoleEntity role = new SysRoleEntity()
                .setValue(SysConstant.SUPER_ADMIN_ROLE_VALUE);
        role.setId(SysConstant.SUPER_ADMIN_ROLE_ID);
        return new UserRoleContainer(Set.of(SysConstant.SUPER_ADMIN_ROLE_ID), List.of(role));
    }

    private UserRoleContainer nonAdminContainer() {
        SysRoleEntity role = new SysRoleEntity()
                .setValue("Operator");
        role.setId(2L);
        return new UserRoleContainer(Set.of(2L), List.of(role));
    }

    private UserDeptContainer deptContainer(Long deptId) {
        SysDeptEntity dept = new SysDeptEntity()
                .setTitle("测试部门");
        dept.setId(deptId);
        return new UserDeptContainer(List.of(deptId), List.of(dept));
    }

    private UserDeptContainer emptyDeptContainer() {
        return new UserDeptContainer(Collections.emptyList(), Collections.emptyList());
    }
}
