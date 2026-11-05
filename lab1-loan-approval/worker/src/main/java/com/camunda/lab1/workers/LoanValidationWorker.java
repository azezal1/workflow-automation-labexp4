package com.camunda.lab1.workers;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Zeebe Job Workers for Loan Approval Process
 * Handles applicant validation and notification tasks
 */
@Component
public class LoanValidationWorker {

    private static final Logger logger = LoggerFactory.getLogger(LoanValidationWorker.class);

    /**
     * Job Worker: Validate Applicant
     * Validates loan application data including applicant details and loan amount
     */
    @JobWorker(type = "validate-applicant")
    public Map<String, Object> validateApplicant(
            @Variable(name = "applicantId") String applicantId,
            @Variable(name = "applicantName") String applicantName,
            @Variable(name = "requestedAmount") Double requestedAmount,
            @Variable(name = "monthlyIncome") Double monthlyIncome,
            @Variable(name = "employmentStatus") String employmentStatus,
            ActivatedJob job) {

        logger.info("Starting validation for job key: {}", job.getKey());
        logger.info("Applicant ID: {}, Name: {}, Amount: {}, Income: {}, Employment: {}",
                applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus);

        List<String> errors = new ArrayList<>();
        Map<String, Object> result = new HashMap<>();

        // Validation rules
        if (applicantId == null || applicantId.trim().isEmpty()) {
            errors.add("Applicant ID is required");
        }

        if (applicantName == null || applicantName.trim().isEmpty()) {
            errors.add("Applicant name is required");
        }

        if (requestedAmount == null || requestedAmount <= 0) {
            errors.add("Requested amount must be greater than 0");
        } else if (requestedAmount > 1000000) {
            errors.add("Requested amount exceeds maximum limit of 1,000,000");
        }

        if (monthlyIncome == null || monthlyIncome <= 0) {
            errors.add("Monthly income must be greater than 0");
        }

        if (employmentStatus == null || employmentStatus.trim().isEmpty()) {
            errors.add("Employment status is required");
        }

        // Set validation results
        if (errors.isEmpty()) {
            result.put("validationPassed", true);
            result.put("validationErrors", null);
            logger.info("Validation PASSED for applicant: {}", applicantId);
        } else {
            result.put("validationPassed", false);
            result.put("validationErrors", String.join(", ", errors));
            logger.warn("Validation FAILED for applicant: {}. Errors: {}", applicantId, errors);
        }

        logger.info("Validation complete for job key: {}", job.getKey());
        return result;
    }

    /**
     * Job Worker: Send Notification
     * Logs notification for loan application decision
     */
    @JobWorker(type = "send-notification")
    public Map<String, Object> sendNotification(
            @Variable(name = "applicantId") String applicantId,
            @Variable(name = "applicantName") String applicantName,
            @Variable(name = "decision") String decision,
            @Variable(name = "requestedAmount") Double requestedAmount,
            ActivatedJob job) {

        logger.info("Sending notification for job key: {}", job.getKey());
        logger.info("========================================");
        logger.info("LOAN APPLICATION NOTIFICATION");
        logger.info("========================================");
        logger.info("Applicant ID: {}", applicantId);
        logger.info("Applicant Name: {}", applicantName);
        logger.info("Requested Amount: ${}", requestedAmount);
        logger.info("Decision: {}", decision);
        logger.info("========================================");

        Map<String, Object> result = new HashMap<>();
        result.put("notificationSent", true);
        result.put("notificationTimestamp", System.currentTimeMillis());
        result.put("notificationMessage", String.format(
                "Notification sent to applicant %s (%s): Loan application decision is %s",
                applicantName, applicantId, decision));

        logger.info("Notification complete for job key: {}", job.getKey());
        return result;
    }
}
