package com.travelplan.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.travelplan.ecommerce", "com.travelplan.common"})
public class EcommerceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceServiceApplication.class, args);
    }
}
