package com.travelplan.guide;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(
    basePackages = {"com.travelplan.guide", "com.travelplan.common"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = com.travelplan.common.exception.GlobalExceptionHandler.class
    )
)
public class TourGuideServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourGuideServiceApplication.class, args);
    }
}
