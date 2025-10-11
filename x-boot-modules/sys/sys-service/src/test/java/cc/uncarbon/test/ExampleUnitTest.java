package io.github.test;

import io.github.framework.core.constant.BaseConstant;
import io.github.framework.core.context.TenantContext;
import io.github.framework.core.context.TenantContextHolder;
import io.github.framework.core.context.UserContext;
import io.github.framework.core.context.UserContextHolder;
import io.github.module.sys.SysServiceApplication;
import io.github.module.sys.facade.SysRoleFacade;
import io.github.module.sys.model.response.SysRoleBO;
import cn.hutool.core.collection.CollUtil;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * 一个仅用于示例的单元测试
 */
@SpringBootTest(classes = SysServiceApplication.class)
class ExampleUnitTest {

    @Resource
    private SysRoleFacade sysRoleFacade;


    @BeforeAll
    public static void init() {
        // 设置用户上下文
        UserContext userContext = new UserContext();
        userContext
                .setUserId(1L)
                .setUserName("超级管理员")
                // 用户类型, 根据单元测试需要修改
                .setUserTypeStr("ADMIN_USER");
        UserContextHolder.setUserContext(userContext);

        // 设置租户上下文
        TenantContext tenantContext = new TenantContext();
        tenantContext
                .setTenantId(BaseConstant.Tenant.DEFAULT_PRIVILEGED_TENANT_ID)
                .setTenantName("超级租户");
        TenantContextHolder.setTenantContext(tenantContext);
    }

    @Test
    void exampleTest() {
        List<SysRoleBO> selectOptions = sysRoleFacade.adminSelectOptions();
        Assertions.assertTrue(CollUtil.isNotEmpty(selectOptions));
    }
}
