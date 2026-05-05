package com.geek.threaddoctor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ThreadDoctorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ThreadDoctorApplication.class, args);
    }
}
