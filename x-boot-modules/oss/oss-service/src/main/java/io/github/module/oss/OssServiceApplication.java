package io.github.module.oss;

import io.github.framework.crud.annotation.EnableInitHikariPoolAtStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@EnableInitHikariPoolAtStartup
@SpringBootApplication
public class OssServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OssServiceApplication.class, args);
    }

}
