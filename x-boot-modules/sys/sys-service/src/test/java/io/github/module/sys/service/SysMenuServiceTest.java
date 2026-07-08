package io.github.module.sys.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.framework.core.enums.EnabledStatusEnum;
import io.github.module.sys.constant.SysConstant;
import io.github.module.sys.entity.SysMenuEntity;
import io.github.module.sys.enums.SysMenuTypeEnum;
import io.github.module.sys.mapper.SysMenuMapper;
import io.github.module.sys.model.request.AdminInsertOrUpdateSysMenuDTO;
import io.github.module.sys.model.response.SysMenuBO;
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
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysMenuServiceTest {

    private static final Long MENU_ID = 2L;

    @Mock
    private SysMenuMapper sysMenuMapper;

    @Mock
    private SysRoleMenuRelationService sysRoleMenuRelationService;

    @InjectMocks
    private SysMenuService sysMenuService;

    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                SysMenuEntity.class
        );
    }

    @BeforeEach
    void setUp() {
        UserContextHolder.setUserContext(new UserContext()
                .setUserId(1L)
                .setUserName("superadmin")
                .setUserTypeStr("ADMIN_USER")
                .setRolesIds(Set.of(SysConstant.SUPER_ADMIN_ROLE_ID)));
    }

    @AfterEach
    void tearDown() {
        UserContextHolder.clear();
    }

    @Test
    void adminInsertCreatesMenuWithRootParentAndCleanPermission() {
        AdminInsertOrUpdateSysMenuDTO dto = validDto()
                .setParentId(null)
                .setPermission(" sys:user:list ");
        when(sysMenuMapper.selectOne(any())).thenReturn(null);
        when(sysMenuMapper.insert(any(SysMenuEntity.class))).thenAnswer(invocation -> {
            SysMenuEntity entity = invocation.getArgument(0);
            entity.setId(MENU_ID);
            return 1;
        });

        Long menuId = sysMenuService.adminInsert(dto);

        ArgumentCaptor<SysMenuEntity> entityCaptor = ArgumentCaptor.forClass(SysMenuEntity.class);
        verify(sysMenuMapper).insert(entityCaptor.capture());
        SysMenuEntity inserted = entityCaptor.getValue();
        assertThat(menuId).isEqualTo(MENU_ID);
        assertThat(dto.getId()).isNull();
        assertThat(dto.getParentId()).isEqualTo(SysConstant.ROOT_PARENT_ID);
        assertThat(inserted.getTitle()).isEqualTo("用户管理");
        assertThat(inserted.getParentId()).isEqualTo(SysConstant.ROOT_PARENT_ID);
        assertThat(inserted.getPermission()).isEqualTo("sys:user:list");
    }

    @Test
    void adminUpdateUpdatesMenuWithRootParent() {
        AdminInsertOrUpdateSysMenuDTO dto = validDto()
                .setId(MENU_ID)
                .setParentId(null)
                .setTitle("账号管理")
                .setPermission("sys:user:update");
        when(sysMenuMapper.selectOne(any())).thenReturn(menu(MENU_ID, "账号管理", SysMenuTypeEnum.MENU));

        sysMenuService.adminUpdate(dto);

        ArgumentCaptor<SysMenuEntity> entityCaptor = ArgumentCaptor.forClass(SysMenuEntity.class);
        verify(sysMenuMapper).updateById(entityCaptor.capture());
        SysMenuEntity updated = entityCaptor.getValue();
        assertThat(updated.getId()).isEqualTo(MENU_ID);
        assertThat(updated.getTitle()).isEqualTo("账号管理");
        assertThat(updated.getParentId()).isEqualTo(SysConstant.ROOT_PARENT_ID);
        assertThat(updated.getPermission()).isEqualTo("sys:user:update");
    }

    @Test
    void adminDeleteDeletesMenusByIds() {
        sysMenuService.adminDelete(List.of(MENU_ID, 3L));

        verify(sysMenuMapper).deleteBatchIds(List.of(MENU_ID, 3L));
    }

    @Test
    void adminListSideMenuReturnsOnlySideMenuTypesForSuperAdmin() {
        when(sysMenuMapper.selectList(any()))
                .thenReturn(allEnabledMenus())
                .thenReturn(List.of(
                        menu(1L, "系统管理", SysMenuTypeEnum.DIR),
                        menu(MENU_ID, "用户管理", SysMenuTypeEnum.MENU),
                        externalLinkMenu(4L)
                ));

        List<SysMenuBO> sideMenus = sysMenuService.adminListSideMenu();

        assertThat(sideMenus).extracting(SysMenuBO::getType)
                .containsExactly(SysMenuTypeEnum.DIR, SysMenuTypeEnum.MENU, SysMenuTypeEnum.EXTERNAL_LINK);
        assertThat(sideMenus).noneMatch(menu -> SysMenuTypeEnum.BUTTON.equals(menu.getType()));
        assertThat(sideMenus.getFirst().getParentId()).isNull();
        assertThat(sideMenus.getFirst().getComponent()).isEqualTo(SysConstant.VBEN_ADMIN_BLANK_VIEW);
        assertThat(sideMenus.get(1).getPath()).isEqualTo("/sys/user/index");
        assertThat(sideMenus.get(2).getComponent()).isEqualTo("https://example.com");
    }

    @Test
    void adminListVisibleMenuReturnsAllMenuTypesForSuperAdmin() {
        when(sysMenuMapper.selectList(any()))
                .thenReturn(allEnabledMenus())
                .thenReturn(allEnabledMenus());

        List<SysMenuBO> visibleMenus = sysMenuService.adminListVisibleMenu();

        assertThat(visibleMenus).extracting(SysMenuBO::getType)
                .containsExactly(
                        SysMenuTypeEnum.DIR,
                        SysMenuTypeEnum.MENU,
                        SysMenuTypeEnum.BUTTON,
                        SysMenuTypeEnum.EXTERNAL_LINK
                );
        SysMenuBO button = visibleMenus.get(2);
        assertThat(button.getComponent()).isEqualTo(SysConstant.VBEN_ADMIN_BLANK_VIEW);
        assertThat(button.getPath()).startsWith("/");
        assertThat(button.getPermission()).isEqualTo("sys:user:add");
    }

    @Test
    void getRoleIdPermissionMapForSuperAdminReadsEveryConfiguredPermission() {
        when(sysMenuMapper.selectList(null)).thenReturn(List.of(
                permissionMenu(10L, "SysUser:retrieve", EnabledStatusEnum.ENABLED),
                permissionMenu(11L, "SysUser:create", EnabledStatusEnum.DISABLED),
                permissionMenu(12L, "", EnabledStatusEnum.ENABLED),
                permissionMenu(13L, null, EnabledStatusEnum.ENABLED)
        ));

        Map<Long, Set<String>> rolePermissions =
                sysMenuService.getRoleIdPermissionMap(Set.of(SysConstant.SUPER_ADMIN_ROLE_ID));

        assertThat(rolePermissions).containsOnlyKeys(SysConstant.SUPER_ADMIN_ROLE_ID);
        assertThat(rolePermissions.get(SysConstant.SUPER_ADMIN_ROLE_ID))
                .containsExactlyInAnyOrder("SysUser:retrieve", "SysUser:create");
        verifyNoInteractions(sysRoleMenuRelationService);
    }

    @Test
    void getRoleIdPermissionMapForNormalRoleReadsBoundMenuPermissions() {
        Long roleId = 20L;
        when(sysRoleMenuRelationService.listMenuIdsByRoleIds(Set.of(roleId)))
                .thenReturn(Set.of(10L, 11L, 12L));
        when(sysMenuMapper.selectList(any())).thenReturn(List.of(
                permissionMenu(10L, "SysUser:retrieve", EnabledStatusEnum.ENABLED),
                permissionMenu(11L, "SysUser:create", EnabledStatusEnum.ENABLED),
                permissionMenu(12L, "", EnabledStatusEnum.ENABLED)
        ));

        Map<Long, Set<String>> rolePermissions = sysMenuService.getRoleIdPermissionMap(Set.of(roleId));

        assertThat(rolePermissions).containsOnlyKeys(roleId);
        assertThat(rolePermissions.get(roleId))
                .containsExactlyInAnyOrder("SysUser:retrieve", "SysUser:create");
    }

    @Test
    void listPermissionsByMenuIdsReturnsNonBlankMenuPermissions() {
        when(sysMenuMapper.selectList(any())).thenReturn(List.of(
                permissionMenu(10L, "SysRole:retrieve", EnabledStatusEnum.ENABLED),
                permissionMenu(11L, "", EnabledStatusEnum.ENABLED),
                permissionMenu(12L, null, EnabledStatusEnum.ENABLED)
        ));

        Set<String> permissions = sysMenuService.listPermissionsByMenuIds(Set.of(10L, 11L, 12L));

        assertThat(permissions).containsExactly("SysRole:retrieve");
    }

    private AdminInsertOrUpdateSysMenuDTO validDto() {
        return AdminInsertOrUpdateSysMenuDTO.builder()
                .title("用户管理")
                .type(SysMenuTypeEnum.MENU)
                .component("sys/user/index")
                .permission("sys:user:list")
                .icon("user")
                .sort(10)
                .status(EnabledStatusEnum.ENABLED)
                .build();
    }

    private List<SysMenuEntity> allEnabledMenus() {
        return List.of(
                menu(1L, "系统管理", SysMenuTypeEnum.DIR),
                menu(MENU_ID, "用户管理", SysMenuTypeEnum.MENU),
                buttonMenu(3L),
                externalLinkMenu(4L)
        );
    }

    private SysMenuEntity menu(Long id, String title, SysMenuTypeEnum type) {
        SysMenuEntity entity = new SysMenuEntity()
                .setTitle(title)
                .setParentId(SysConstant.ROOT_PARENT_ID)
                .setType(type)
                .setComponent(SysMenuTypeEnum.MENU.equals(type) ? "sys/user/index" : null)
                .setPermission(SysMenuTypeEnum.MENU.equals(type) ? "sys:user:list" : null)
                .setIcon("menu")
                .setSort(id.intValue())
                .setStatus(EnabledStatusEnum.ENABLED);
        entity.setId(id);
        return entity;
    }

    private SysMenuEntity buttonMenu(Long id) {
        SysMenuEntity entity = menu(id, "新增用户", SysMenuTypeEnum.BUTTON)
                .setPermission("sys:user:add");
        entity.setParentId(MENU_ID);
        return entity;
    }

    private SysMenuEntity permissionMenu(Long id, String permission, EnabledStatusEnum status) {
        SysMenuEntity entity = menu(id, "权限节点", SysMenuTypeEnum.BUTTON)
                .setPermission(permission)
                .setStatus(status);
        entity.setParentId(MENU_ID);
        return entity;
    }

    private SysMenuEntity externalLinkMenu(Long id) {
        SysMenuEntity entity = menu(id, "帮助文档", SysMenuTypeEnum.EXTERNAL_LINK)
                .setExternalLink("https://example.com");
        entity.setParentId(SysConstant.ROOT_PARENT_ID);
        return entity;
    }
}
