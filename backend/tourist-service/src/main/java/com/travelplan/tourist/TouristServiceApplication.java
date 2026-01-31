package com.travelplan.tourist;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.travelplan.tourist", "com.travelplan.common"})
public class TouristServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TouristServiceApplication.class, args);
    }
}
