package com.camunda.lab3;

import io.camunda.zeebe.spring.client.annotation.Deployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application for Hospital Appointment and Patient Triage Worker
 * Lab 3: End-to-End Hospital Appointment and Patient Triage Management
 */
@SpringBootApplication
public class AppointmentWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AppointmentWorkerApplication.class, args);
    }
}
