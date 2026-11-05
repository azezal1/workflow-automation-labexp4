package com.camunda.lab1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application for Camunda 8 Loan Approval Worker
 * Lab 1: End-to-End Loan Approval Automation
 */
@SpringBootApplication
public class LoanWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanWorkerApplication.class, args);
    }
}
