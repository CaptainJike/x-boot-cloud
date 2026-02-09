package io.github.framework.crud.service;

import io.github.framework.crud.entity.BaseEntity;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务基础模板
 * @param <E> 实体类
 */
public interface XBootBaseService<E extends BaseEntity<?>>
        extends IService<E> {

}
