package io.github.module.adminapi.web.sys;

import io.github.framework.web.model.request.IdsDTO;
import io.github.framework.web.model.response.ApiResult;
import io.github.module.adminapi.helper.RolePermissionCacheHelper;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.request.AdminBindRoleMenuRelationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminSysRoleControllerTest {

    @Mock
    private RolePermissionCacheHelper rolePermissionCacheHelper;

    @Mock
    private SysRoleFacade sysRoleFacade;

    private AdminSysRoleController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminSysRoleController(rolePermissionCacheHelper);
        ReflectionTestUtils.setField(controller, "sysRoleFacade", sysRoleFacade);
    }

    @Test
    void bindMenusRefreshesRolePermissionCacheAfterFacadeUpdate() {
        AdminBindRoleMenuRelationDTO dto = AdminBindRoleMenuRelationDTO.builder()
                .menuIds(Set.of(11L, 12L))
                .build();
        Set<String> permissions = Set.of("SysUser:retrieve", "SysRole:update");
        when(sysRoleFacade.adminBindMenus(dto)).thenReturn(permissions);

        ApiResult<Void> result = controller.bindMenus(9L, dto);

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(dto.getRoleId()).isEqualTo(9L);
        InOrder inOrder = inOrder(sysRoleFacade, rolePermissionCacheHelper);
        inOrder.verify(sysRoleFacade).adminBindMenus(dto);
        inOrder.verify(rolePermissionCacheHelper).putCache(9L, permissions);
    }

    @Test
    void deleteRemovesRolePermissionCacheAfterFacadeDelete() {
        List<Long> roleIds = List.of(7L, 8L);
        IdsDTO<Long> dto = new IdsDTO<Long>().setIds(roleIds);

        ApiResult<Void> result = controller.delete(dto);

        assertThat(result.getCode()).isEqualTo(200);
        InOrder inOrder = inOrder(sysRoleFacade, rolePermissionCacheHelper);
        inOrder.verify(sysRoleFacade).adminDelete(roleIds);
        inOrder.verify(rolePermissionCacheHelper).deleteCache(roleIds);
    }
}
