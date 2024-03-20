package com.rentalcar.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication
@EnableJpaAuditing
@EnableWebMvc
public class RentalCarServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalCarServerApplication.class, args);
    }

}
