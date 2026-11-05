package com.camunda.lab3.workers;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Zeebe Job Worker for Hospital Appointment Registration and Booking
 * Handles validation, booking, error handling, and notifications
 */
@Component
public class RegistrationWorker {

    private static final Logger logger = LoggerFactory.getLogger(RegistrationWorker.class);
    
    // Pattern for patient reference: 2 uppercase letters followed by 6 digits
    private static final Pattern PATIENT_REF_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{6}$");
    
    // Valid urgency indicator values
    private static final String[] VALID_URGENCY_INDICATORS = {"LOW", "MEDIUM", "HIGH", "CRITICAL", "EMERGENCY"};

    /**
     * (a) Validate Registration
     * Validates patient registration details including mandatory fields and format checks
     */
    @JobWorker(type = "validate-registration")
    public Map<String, Object> validateRegistration(ActivatedJob job) {
        logger.info("Processing validate-registration job for instance: {}", job.getProcessInstanceKey());
        
        Map<String, Object> variables = new HashMap<>();
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        try {
            // Extract input variables
            String patientRef = getVariableAsString(job, "patientRef");
            String patientName = getVariableAsString(job, "patientName");
            String appointmentPreference = getVariableAsString(job, "appointmentPreference");
            String symptomsDescription = getVariableAsString(job, "symptomsDescription");
            String urgencyIndicators = getVariableAsString(job, "urgencyIndicators");

            logger.info("Validating registration for patient: {} ({})", patientName, patientRef);

            // Validate patientRef is not blank
            if (patientRef == null || patientRef.trim().isEmpty()) {
                errors.append("Patient reference is mandatory. ");
                isValid = false;
            } else if (!PATIENT_REF_PATTERN.matcher(patientRef.trim()).matches()) {
                errors.append("Patient reference must match pattern [A-Z]{2}[0-9]{6}. ");
                isValid = false;
            }

            // Validate patientName is not blank
            if (patientName == null || patientName.trim().isEmpty()) {
                errors.append("Patient name is mandatory. ");
                isValid = false;
            }

            // Validate appointmentPreference is not blank
            if (appointmentPreference == null || appointmentPreference.trim().isEmpty()) {
                errors.append("Appointment preference is mandatory. ");
                isValid = false;
            }

            // Validate symptomsDescription is not blank
            if (symptomsDescription == null || symptomsDescription.trim().isEmpty()) {
                errors.append("Symptoms description is mandatory. ");
                isValid = false;
            }

            // Validate urgencyIndicators is valid
            if (urgencyIndicators == null || urgencyIndicators.trim().isEmpty()) {
                errors.append("Urgency indicator is mandatory. ");
                isValid = false;
            } else {
                boolean validUrgency = false;
                String urgencyUpper = urgencyIndicators.trim().toUpperCase();
                for (String valid : VALID_URGENCY_INDICATORS) {
                    if (valid.equals(urgencyUpper)) {
                        validUrgency = true;
                        break;
                    }
                }
                if (!validUrgency) {
                    errors.append("Urgency indicator must be one of: LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY. ");
                    isValid = false;
                }
            }

            // Set output variables
            variables.put("registrationValid", isValid);
            variables.put("validationErrors", isValid ? "" : errors.toString().trim());

            if (isValid) {
                logger.info("Registration validation PASSED for patient: {}", patientRef);
            } else {
                logger.warn("Registration validation FAILED for patient: {}. Errors: {}", patientRef, errors.toString());
            }

        } catch (Exception e) {
            logger.error("Error during registration validation", e);
            variables.put("registrationValid", false);
            variables.put("validationErrors", "Validation error: " + e.getMessage());
        }

        return variables;
    }

    /**
     * (b) Book Appointment
     * Books the appointment using the selected slot and patient details
     */
    @JobWorker(type = "book-appointment")
    public Map<String, Object> bookAppointment(ActivatedJob job) {
        logger.info("Processing book-appointment job for instance: {}", job.getProcessInstanceKey());
        
        Map<String, Object> variables = new HashMap<>();

        try {
            // Extract input variables
            String patientRef = getVariableAsString(job, "patientRef");
            String slotId = getVariableAsString(job, "slotId");
            String triageCategory = getVariableAsString(job, "triageCategory");

            logger.info("Booking appointment for patient: {}, Slot: {}, Category: {}", 
                       patientRef, slotId, triageCategory);

            // Generate booking confirmation
            String bookingConfirmation = "BOOK-" + patientRef + "-" + System.currentTimeMillis();
            
            // Set output variables
            variables.put("bookingStatus", "CONFIRMED");
            variables.put("bookingConfirmation", bookingConfirmation);

            logger.info("Appointment booked successfully. Confirmation: {}", bookingConfirmation);

        } catch (Exception e) {
            logger.error("Error during appointment booking", e);
            variables.put("bookingStatus", "FAILED");
            variables.put("bookingConfirmation", "");
        }

        return variables;
    }

    /**
     * (c) Handle Registration Error
     * Handles registration validation errors and logs notification
     */
    @JobWorker(type = "handle-registration-error")
    public Map<String, Object> handleRegistrationError(ActivatedJob job) {
        logger.info("Processing handle-registration-error job for instance: {}", job.getProcessInstanceKey());
        
        Map<String, Object> variables = new HashMap<>();

        try {
            String patientRef = getVariableAsString(job, "patientRef");
            String validationErrors = getVariableAsString(job, "validationErrors");

            logger.error("Registration error for patient: {}. Errors: {}", patientRef, validationErrors);
            logger.info("Sending error notification to patient: {}", patientRef);

            // Set notification sent flag
            variables.put("notificationSent", true);

        } catch (Exception e) {
            logger.error("Error handling registration error notification", e);
            variables.put("notificationSent", false);
        }

        return variables;
    }

    /**
     * (d) Send Triage Notification
     * Sends notification about triage result to patient
     */
    @JobWorker(type = "send-triage-notification")
    public Map<String, Object> sendTriageNotification(ActivatedJob job) {
        logger.info("Processing send-triage-notification job for instance: {}", job.getProcessInstanceKey());
        
        Map<String, Object> variables = new HashMap<>();

        try {
            String patientRef = getVariableAsString(job, "patientRef");
            String triageCategory = getVariableAsString(job, "triageCategory");
            String bookingStatus = getVariableAsString(job, "bookingStatus");

            logger.info("Triage result for patient {}: Category={}, BookingStatus={}", 
                       patientRef, triageCategory, bookingStatus);
            logger.info("Sending triage notification to patient: {}", patientRef);

            // Set notification sent flag
            variables.put("notificationSent", true);

        } catch (Exception e) {
            logger.error("Error sending triage notification", e);
            variables.put("notificationSent", false);
        }

        return variables;
    }

    /**
     * Helper method to safely extract string variables from job
     */
    private String getVariableAsString(ActivatedJob job, String variableName) {
        try {
            Object value = job.getVariablesAsMap().get(variableName);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            logger.warn("Could not extract variable '{}': {}", variableName, e.getMessage());
            return "";
        }
    }
}
