package io.github.framework.web.jackson;

import io.github.framework.core.constant.BaseConstant;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 自定义序列化规则
 **/
public class BaseJacksonModule extends SimpleModule {

    public BaseJacksonModule() {
        super();
        /*
        大整数转字符串, 避免精度丢失问题
         */
        this.addSerializer(Long.class, ToStringSerializer.instance);
        this.addSerializer(Long.TYPE, ToStringSerializer.instance);
        this.addSerializer(BigInteger.class, ToStringSerializer.instance);
        this.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        /*
        时间相关；时区跟随JVM设置
        */
        // knife4j-aggression用的hutool 5.4.1版本还没有DatePattern.*FORMATTER常量，为了兼容只能先手动构造了
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(BaseConstant.Jackson.DATE_TIME_FORMAT);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(BaseConstant.Jackson.DATE_FORMAT);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern(BaseConstant.Jackson.TIME_FORMAT);
        this.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        this.addSerializer(LocalDate.class, new LocalDateSerializer(dateFormatter));
        this.addSerializer(LocalTime.class, new LocalTimeSerializer(timeFormatter));
        this.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        this.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateFormatter));
        this.addDeserializer(LocalTime.class, new LocalTimeDeserializer(timeFormatter));
    }

}
