package com.travelplan.vehicle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(basePackages = { "com.travelplan.vehicle",
        "com.travelplan.common" }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
                // This tells Spring: "Ignore any file in 'common' that ends with
                // 'SecurityConfig'"
                pattern = "com\\.travelplan\\.common\\..*SecurityConfig"))
public class VehicleServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VehicleServiceApplication.class, args);
    }
}