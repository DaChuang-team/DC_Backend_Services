package org.dachuang_team.dc_backend_services;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DcBackendServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(DcBackendServicesApplication.class, args);
    }

}
