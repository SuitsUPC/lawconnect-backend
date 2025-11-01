package com.qu3dena.lawconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.qu3dena.lawconnect.backend")
@EnableJpaRepositories(basePackages = "com.qu3dena.lawconnect.backend.cases.infrastructure.persistence.jpa.repositories")
@EnableJpaAuditing
public class CasesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CasesServiceApplication.class, args);
    }

}

