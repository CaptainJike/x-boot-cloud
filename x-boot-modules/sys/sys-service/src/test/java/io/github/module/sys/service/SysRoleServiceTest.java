package io.github.module.sys.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.sys.constant.SysConstant;
import io.github.module.sys.entity.SysRoleEntity;
import io.github.module.sys.mapper.SysRoleMapper;
import io.github.module.sys.model.request.AdminBindRoleMenuRelationDTO;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysRoleDTO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceTest {

    private static final Long CURRENT_USER_ID = 1L;
    private static final Long TARGET_ROLE_ID = 2L;

    @Mock
    private SysRoleMapper sysRoleMapper;

    @Mock
    private SysUserRoleRelationService sysUserRoleRelationService;

    @Mock
    private SysRoleMenuRelationService sysRoleMenuRelationService;

    @Mock
    private SysMenuService sysMenuService;

    @InjectMocks
    private SysRoleService sysRoleService;

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SysRoleEntity.class
        );
    }

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
    void adminInsertCreatesRole() {
        AdminInsertOrUpdateSysRoleDTO dto = validDto();
        when(sysRoleMapper.selectOne(any())).thenReturn(null);
        when(sysRoleMapper.insert(any(SysRoleEntity.class))).thenAnswer(invocation -> {
            SysRoleEntity entity = invocation.getArgument(0);
            entity.setId(TARGET_ROLE_ID);
            return 1;
        });

        Long roleId = sysRoleService.adminInsert(dto);

        ArgumentCaptor<SysRoleEntity> entityCaptor = ArgumentCaptor.forClass(SysRoleEntity.class);
        verify(sysRoleMapper).insert(entityCaptor.capture());
        SysRoleEntity inserted = entityCaptor.getValue();
        assertThat(roleId).isEqualTo(TARGET_ROLE_ID);
        assertThat(dto.getId()).isNull();
        assertThat(inserted.getTitle()).isEqualTo("运营角色");
        assertThat(inserted.getValue()).isEqualTo("Operator");
    }

    @Test
    void adminUpdateUpdatesEditableRole() {
        AdminInsertOrUpdateSysRoleDTO dto = validDto()
                .setId(TARGET_ROLE_ID)
                .setTitle("运营主管")
                .setValue("OperatorLead");
        when(sysRoleMapper.selectById(TARGET_ROLE_ID)).thenReturn(role(TARGET_ROLE_ID, "运营角色", "Operator"));
        when(sysRoleMapper.selectOne(any())).thenReturn(role(TARGET_ROLE_ID, "运营主管", "OperatorLead"));

        sysRoleService.adminUpdate(dto);

        ArgumentCaptor<SysRoleEntity> entityCaptor = ArgumentCaptor.forClass(SysRoleEntity.class);
        verify(sysRoleMapper).updateById(entityCaptor.capture());
        SysRoleEntity updated = entityCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(TARGET_ROLE_ID);
        assertThat(updated.getTitle()).isEqualTo("运营主管");
        assertThat(updated.getValue()).isEqualTo("OperatorLead");
    }

    @Test
    void adminDeleteDeletesAllowedRoles() {
        when(sysRoleMapper.selectBatchIds(List.of(TARGET_ROLE_ID, 3L)))
                .thenReturn(List.of(role(TARGET_ROLE_ID, "运营角色", "Operator"), role(3L, "审计角色", "Auditor")));
        mockCurrentUserAsSuperAdmin();

        sysRoleService.adminDelete(List.of(TARGET_ROLE_ID, 3L));

        verify(sysRoleMapper).deleteBatchIds(List.of(TARGET_ROLE_ID, 3L));
    }

    @Test
    void adminBindMenusBindsMenusAndReturnsPermissions() {
        AdminBindRoleMenuRelationDTO dto = AdminBindRoleMenuRelationDTO.builder()
                .roleId(TARGET_ROLE_ID)
                .menuIds(Set.of(10L, 11L))
                .build();
        mockCurrentUserAsSuperAdmin();
        when(sysRoleMapper.selectById(TARGET_ROLE_ID)).thenReturn(role(TARGET_ROLE_ID, "运营角色", "Operator"));
        when(sysMenuService.listPermissionsByMenuIds(Set.of(10L, 11L)))
                .thenReturn(Set.of("sys:user:list", "sys:user:update"));

        Set<String> permissions = sysRoleService.adminBindMenus(dto);

        assertThat(permissions).containsExactlyInAnyOrder("sys:user:list", "sys:user:update");
        verify(sysRoleMenuRelationService).cleanAndBind(TARGET_ROLE_ID, Set.of(10L, 11L));
    }

    private AdminInsertOrUpdateSysRoleDTO validDto() {
        return AdminInsertOrUpdateSysRoleDTO.builder()
                .title("运营角色")
                .value("Operator")
                .build();
    }

    private void mockCurrentUserAsSuperAdmin() {
        when(sysUserRoleRelationService.listRoleIdsByUserId(CURRENT_USER_ID))
                .thenReturn(Set.of(SysConstant.SUPER_ADMIN_ROLE_ID));
        when(sysRoleMapper.selectBatchIds(Set.of(SysConstant.SUPER_ADMIN_ROLE_ID)))
                .thenReturn(List.of(superAdminRole()));
    }

    private SysRoleEntity superAdminRole() {
        return role(SysConstant.SUPER_ADMIN_ROLE_ID, "超级管理员", SysConstant.SUPER_ADMIN_ROLE_VALUE);
    }

    private SysRoleEntity role(Long id, String title, String value) {
        SysRoleEntity entity = new SysRoleEntity()
                .setTitle(title)
                .setValue(value);
        entity.setId(id);
        return entity;
    }
}
