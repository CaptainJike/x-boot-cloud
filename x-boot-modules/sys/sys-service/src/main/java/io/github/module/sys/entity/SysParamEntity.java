package io.github.module.sys.entity;

import io.github.framework.crud.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;


/**
 * 系统参数
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName(value = "sys_param")
public class SysParamEntity extends BaseEntity<Long> {

	@Schema(description = "键名")
	@TableField(value = "name")
	private String name;

	@Schema(description = "键值")
	@TableField(value = "value")
	private String value;

	@Schema(description = "描述")
	@TableField(value = "description")
	private String description;

}
