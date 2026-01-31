package com.travelplan.guide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.travelplan.guide", "com.travelplan.common"})
public class TourGuideServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourGuideServiceApplication.class, args);
    }
}
