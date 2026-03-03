package com.travelplan.tripplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(
    basePackages = {"com.travelplan.tripplan", "com.travelplan.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {
            com.travelplan.common.config.CommonSecurityConfig.class,
            com.travelplan.common.config.CorsConfig.class
        }
    )
)
public class TripPlanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripPlanServiceApplication.class, args);
    }
}
