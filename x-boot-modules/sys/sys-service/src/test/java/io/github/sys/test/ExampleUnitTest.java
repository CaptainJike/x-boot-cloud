package io.github.sys.test;

import io.github.module.sys.biz.SysRoleFacadeImpl;
import io.github.module.sys.model.response.SysRoleBO;
import io.github.module.sys.service.SysRoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色 Facade 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExampleUnitTest {

    @Mock
    private SysRoleService sysRoleService;

    @InjectMocks
    private SysRoleFacadeImpl sysRoleFacade;

    @Test
    void adminSelectOptionsDelegatesToRoleService() {
        SysRoleBO role = SysRoleBO.builder()
                .id(1L)
                .title("超级管理员")
                .build();
        when(sysRoleService.adminSelectOptions()).thenReturn(List.of(role));

        List<SysRoleBO> selectOptions = sysRoleFacade.adminSelectOptions();

        assertThat(selectOptions).containsExactly(role);
        verify(sysRoleService).adminSelectOptions();
    }
}
