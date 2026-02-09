package io.github.framework.crud.handler;

import io.github.framework.core.enums.IdGeneratorStrategyEnum;
import cn.hutool.core.net.NetUtil;
import com.baomidou.mybatisplus.core.incrementer.DefaultIdentifierGenerator;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义ID生成器 - Mybatis-Plus Sequence
 */
@Slf4j
public class BaseSequenceIdGenerateHandler extends DefaultIdentifierGenerator {

    public BaseSequenceIdGenerateHandler() {
        super(NetUtil.getLocalhost());
        log.info("[主键ID生成器] >> strategy=[{}]",
                IdGeneratorStrategyEnum.SEQUENCE
        );
    }
}
