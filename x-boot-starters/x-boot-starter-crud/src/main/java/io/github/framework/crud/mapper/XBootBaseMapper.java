package io.github.framework.crud.mapper;

import io.github.framework.core.function.StreamFunction;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 持久层基础模板，在Mybatis-Plus BaseMapper上进一步增加功能
 *
 * @param <E> 实体类
 */
public interface XBootBaseMapper<E> extends BaseMapper<E> {

    /**
     * 根据IDs查询
     *
     * @deprecated use selectBatchIds()
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    default List<E> selectByIds(Collection<? extends Serializable> ids) {
        return selectBatchIds(ids);
    }

    /**
     * 取ID👉名 映射map
     *
     * @deprecated use selectToMap()
     */
    @Deprecated(since = "2.2.0", forRemoval = true)
    default <T extends Serializable> Map<T, String> getNameMap(Collection<T> ids,
                                                               Function<E, T> idMapper, Function<E, String> nameMapper) {
        List<E> entityList = selectBatchIds(ids);
        if (CollUtil.isEmpty(entityList)) {
            return Collections.emptyMap();
        }
        return entityList.stream()
                .collect(Collectors.toMap(idMapper, nameMapper, StreamFunction.ignoredThrowingMerger()));
    }

    /**
     * 快速构建业务中常用的查库并转map，常用于根据ID补全名称等业务场景
     * 如：selectToMap(ids, Entity::getId)
     */
    default <K> Map<K, E> selectToMap(Collection<K> keys, SFunction<E, K> keyFieldGetter) {
        if (CollUtil.isEmpty(keys)) {
            return Map.of();
        }
        List<E> entityList = selectList(
                new LambdaQueryWrapper<E>()
                        .in(keyFieldGetter, keys)
        );
        if (CollUtil.isEmpty(entityList)) {
            return Map.of();
        }
        return entityList.stream().collect(Collectors.toMap(keyFieldGetter, Function.identity(), StreamFunction.ignoredThrowingMerger()));
    }

    /**
     * 快速构建业务中常用的查库并转map，常用于根据ID补全名称等业务场景
     * 如：selectToMap(ids, Entity::getId, Entity::getName)
     */
    default <K, V> Map<K, V> selectToMap(Collection<K> keys,
                                         SFunction<E, K> keyFieldGetter, SFunction<E, V> valueFieldGetter) {
        if (CollUtil.isEmpty(keys)) {
            return Map.of();
        }
        List<E> entityList = selectList(
                new LambdaQueryWrapper<E>()
                        .select(keyFieldGetter, valueFieldGetter)
                        .in(keyFieldGetter, keys)
        );
        if (CollUtil.isEmpty(entityList)) {
            return Map.of();
        }
        return entityList.stream().collect(Collectors.toMap(keyFieldGetter, valueFieldGetter, StreamFunction.ignoredThrowingMerger()));
    }

    /**
     * 快速构建业务中常用的查库并转map，常用于根据ID补全名称等业务场景
     * 如：selectToMap(ids, GoodsExtBO.class, Entity::getId, Entity::getName, Entity::getType)
     */
    default <K, V, B> Map<K, B> selectToMap(Collection<K> keys, Class<B> javaBeanClass,
                                            SFunction<E, K> keyFieldGetter, SFunction<E, V>... valueFieldGetters) {
        if (CollUtil.isEmpty(keys)) {
            return Map.of();
        }
        SFunction<E, ?>[] getters = ArrayUtil.append(valueFieldGetters, keyFieldGetter);
        List<E> entityList = selectList(
                new LambdaQueryWrapper<E>()
                        .select(getters)
                        .in(keyFieldGetter, keys)
        );
        if (CollUtil.isEmpty(entityList)) {
            return Map.of();
        }
        return entityList.stream().collect(Collectors.toMap(keyFieldGetter, entity -> BeanUtil.copyProperties(entity, javaBeanClass),
                StreamFunction.ignoredThrowingMerger()));
    }

    /**
     * 快速构建业务中常用的查库并转map，常用于一对多的情况下，根据ID补全多个关联值（使用List保存，有序、不去重）等业务场景
     * 如：selectToGroupingMap(ids, Entity::getId, Entity::getRoleId)
     */
    default <K, V> Map<K, List<V>> selectToGroupingMap(Collection<K> keys,
                                                       SFunction<E, K> keyFieldGetter, SFunction<E, V> valueFieldGetter) {
        if (CollUtil.isEmpty(keys)) {
            return Map.of();
        }
        List<E> entityList = selectList(
                new LambdaQueryWrapper<E>()
                        .select(keyFieldGetter, valueFieldGetter)
                        .in(keyFieldGetter, keys)
        );
        if (CollUtil.isEmpty(entityList)) {
            return Map.of();
        }
        return entityList.stream().collect(Collectors.groupingBy(keyFieldGetter, Collectors.mapping(valueFieldGetter, Collectors.toList())));
    }

    /**
     * 快速构建业务中常用的查库并转map，常用于一对多的情况下，根据ID补全多个关联值（使用Set保存，无序、去重）等业务场景
     * 如：selectToGroupingMap(ids, Entity::getId, Entity::getRoleId)
     */
    default <K, V> Map<K, Set<V>> selectToGroupingSetMap(Collection<K> keys,
                                                         SFunction<E, K> keyFieldGetter, SFunction<E, V> valueFieldGetter) {
        if (CollUtil.isEmpty(keys)) {
            return Map.of();
        }
        List<E> entityList = selectList(
                new LambdaQueryWrapper<E>()
                        .select(keyFieldGetter, valueFieldGetter)
                        .in(keyFieldGetter, keys)
        );
        if (CollUtil.isEmpty(entityList)) {
            return Map.of();
        }
        return entityList.stream().collect(Collectors.groupingBy(keyFieldGetter, Collectors.mapping(valueFieldGetter, Collectors.toSet())));
    }
}
