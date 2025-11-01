package com.qu3dena.lawconnect.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LawConnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(LawConnectBackendApplication.class, args);
    }

}
