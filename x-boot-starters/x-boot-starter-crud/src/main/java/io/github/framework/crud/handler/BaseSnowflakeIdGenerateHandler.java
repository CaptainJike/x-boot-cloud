package io.github.framework.crud.handler;

import io.github.framework.core.enums.IdGeneratorStrategyEnum;
import io.github.framework.core.props.BaseProperties;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 自定义ID生成器 - 雪花ID
 */
@Slf4j
public class BaseSnowflakeIdGenerateHandler implements IdentifierGenerator {

    private final Snowflake snowflakeInstance;


    public BaseSnowflakeIdGenerateHandler(BaseProperties baseProperties) {
        long workerId;

        try {
            // 当前机器的局域网IP
            workerId = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            workerId = NetUtil.getLocalhost().hashCode();
        }

        // Hutool的雪花生成器仅支持0-31
        workerId = workerId % 32;
        Long datacenterId = baseProperties.getCrud().getIdGenerator().getDatacenterId();
        String epochDateStr = baseProperties.getCrud().getIdGenerator().getEpochDate();
        Date epochDate = DateUtil.parseDate(epochDateStr);

        log.info("[主键ID生成器] >> strategy=[{}], workerId=[{}], datacenterId=[{}], epochDate=[{}]",
                IdGeneratorStrategyEnum.SNOWFLAKE,
                workerId,
                datacenterId,
                epochDate
        );

        snowflakeInstance = getSnowflake(workerId, datacenterId, epochDate);
    }

    @Override
    public Number nextId(Object entity) {
        return snowflakeInstance.nextId();
    }

    @Override
    public String nextUUID(Object entity) {
        return IdUtil.randomUUID();
    }

    private static Snowflake getSnowflake(long workerId, long datacenterId, Date epochDate) {
        return Singleton.get(Snowflake.class, epochDate, workerId, datacenterId, false);
    }

}
