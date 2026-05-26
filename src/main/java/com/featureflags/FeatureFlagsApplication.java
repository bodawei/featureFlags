package com.featureflags;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

public class FeatureFlagsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeatureFlagsApplication.class, args);
    }

    public static void foo() {
       System.out.println("zoiks, dude.");
}
}
