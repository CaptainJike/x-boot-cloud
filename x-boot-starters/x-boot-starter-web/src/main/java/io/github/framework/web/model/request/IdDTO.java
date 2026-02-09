package io.github.framework.web.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 用于API接口接收参数
 * 兼容Integer Long String等多种类型的主键
 * 注: 记得加 @RequestBody @Valid 注解
 * @param <T> 主键数据类型
 */
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdDTO<T extends Serializable> implements Serializable {

    @Schema(description = "主键ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private T id;

}
