package com.travelplan.tripplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.travelplan.tripplan", "com.travelplan.common"})
public class TripPlanServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TripPlanServiceApplication.class, args);
    }
}
