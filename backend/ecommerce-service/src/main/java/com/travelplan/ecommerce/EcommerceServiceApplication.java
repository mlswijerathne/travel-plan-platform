package com.travelplan.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.travelplan.ecommerce.client")
@ComponentScan(
    basePackages = {"com.travelplan.ecommerce", "com.travelplan.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.travelplan.common.config.SecurityConfig.class,
            com.travelplan.common.config.CorsConfig.class
        }
    )
)
public class EcommerceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceServiceApplication.class, args);
    }
}
