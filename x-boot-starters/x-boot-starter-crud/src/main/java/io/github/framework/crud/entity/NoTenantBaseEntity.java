package io.github.framework.crud.entity;

import io.github.framework.core.constant.BaseConstant;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 基础实体类，去除[行级租户ID]
 * @param <T> 主键类型，一般用 Long
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public abstract class NoTenantBaseEntity<T extends Serializable> extends BaseEntity<T> {

    /**
     * 覆盖掉行级租户ID
     */
    @TableField(value = BaseConstant.CRUD.COLUMN_TENANT_ID, exist = false)
    private Long tenantId;

}
