package io.github.framework.web.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.framework.core.constant.BaseConstant;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.text.SimpleDateFormat;

/**
 * Jackson 自动配置类
 */
@AutoConfiguration
public class JacksonExtendAutoConfiguration {
    /**
     * 全局配置 序列化和反序列化规则
     */
    @Primary
    @Bean
    public ObjectMapper objectMapper() {
            return JsonMapper.builder()
                    // 设置全局日期格式（等价于 simpleDateFormat）
                    .defaultDateFormat(new SimpleDateFormat(BaseConstant.Jackson.DATE_TIME_FORMAT))
                    // 常见配置：禁用日期转时间戳
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    // 注册 JavaTimeModule（处理 LocalDate、LocalDateTime 等）
                    .addModule(new JavaTimeModule())
                    // 可选：其他配置
                    // .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    // .serializationInclusion(JsonInclude.Include.NON_NULL)
                    .build();
    }
}
