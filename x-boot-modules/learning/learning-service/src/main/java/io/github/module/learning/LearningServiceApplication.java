package io.github.module.learning;

import io.github.framework.crud.annotation.EnableInitHikariPoolAtStartup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableInitHikariPoolAtStartup
@SpringBootApplication
public class LearningServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }
}
