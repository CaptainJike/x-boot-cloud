package io.github.framework.crud.config;

import javax.sql.DataSource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;


/**
 * 在项目启动时直接初始化 Hikari 连接池（否则默认为懒加载）
 */
public class InitHikariPoolAtStartupConfiguration {

    @Bean
    public ApplicationRunner runner(DataSource dataSource) {
        return args -> dataSource.getConnection();
    }
}
