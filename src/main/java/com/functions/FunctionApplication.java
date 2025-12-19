package com.functions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.functions", "controller", "service", "repository", "config", "security", "dto", "entity"})
@EnableJpaRepositories(basePackages = "repository")
@EntityScan(basePackages = "entity")
public class FunctionApplication {
    public static void main(String[] args) {
        SpringApplication.run(FunctionApplication.class, args);
    }
}
