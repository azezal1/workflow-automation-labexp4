package com.camunda.lab2.workers;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderValidationWorker {

    private static final Logger logger = LoggerFactory.getLogger(OrderValidationWorker.class);

    // Product catalog: productId -> price
    private static final Map<String, Double> PRODUCT_CATALOG = Map.of(
            "P001", 29.99,
            "P002", 149.99,
            "P003", 499.99
    );

    private static final double DEFAULT_PRICE = 99.99;
    private static final int MAX_STOCK_QUANTITY = 100;

    /**
     * Validates order, checks inventory, and calculates order total.
     * Derives customer category based on order value.
     */
    @JobWorker(type = "validate-order")
    public Map<String, Object> validateOrder(
            @Variable(name = "customerRef") String customerRef,
            @Variable(name = "productId") String productId,
            @Variable(name = "quantity") Integer quantity,
            @Variable(name = "deliveryPreference") String deliveryPreference,
            @Variable(name = "paymentToken") String paymentToken,
            ActivatedJob job) {

        logger.info("Processing validate-order job for customer: {}, product: {}, quantity: {}",
                customerRef, productId, quantity);

        Map<String, Object> variables = new HashMap<>();

        // Validation checks
        boolean isValid = true;
        StringBuilder validationMessage = new StringBuilder();

        if (customerRef == null || customerRef.trim().isEmpty()) {
            isValid = false;
            validationMessage.append("Customer reference is required. ");
        }

        if (productId == null || productId.trim().isEmpty()) {
            isValid = false;
            validationMessage.append("Product ID is required. ");
        }

        if (quantity == null || quantity <= 0) {
            isValid = false;
            validationMessage.append("Quantity must be greater than zero. ");
        }

        if (deliveryPreference == null || deliveryPreference.trim().isEmpty()) {
            isValid = false;
            validationMessage.append("Delivery preference is required. ");
        }

        if (paymentToken == null || paymentToken.trim().isEmpty()) {
            isValid = false;
            validationMessage.append("Payment token is required. ");
        }

        // If basic validation fails, return early
        if (!isValid) {
            variables.put("orderValid", false);
            variables.put("stockAvailable", false);
            variables.put("orderTotal", 0.0);
            variables.put("validationMessage", validationMessage.toString().trim());
            variables.put("customerCategory", "Basic");
            logger.warn("Order validation failed: {}", validationMessage);
            return variables;
        }

        // Calculate order total
        double unitPrice = PRODUCT_CATALOG.getOrDefault(productId, DEFAULT_PRICE);
        double orderTotal = quantity * unitPrice;

        // Check inventory (simulated: stock available if quantity <= 100)
        boolean stockAvailable = quantity <= MAX_STOCK_QUANTITY;

        // Derive customer category based on order total
        String customerCategory;
        if (orderTotal > 500.0) {
            customerCategory = "Premium";
        } else if (orderTotal > 100.0) {
            customerCategory = "Regular";
        } else {
            customerCategory = "Basic";
        }

        // Set validation results
        variables.put("orderValid", true);
        variables.put("stockAvailable", stockAvailable);
        variables.put("orderTotal", orderTotal);
        variables.put("validationMessage", "Order validation successful");
        variables.put("customerCategory", customerCategory);

        logger.info("Order validation completed - Valid: true, Stock: {}, Total: ${}, Category: {}",
                stockAvailable, orderTotal, customerCategory);

        return variables;
    }

    /**
     * Creates fulfilment record for approved orders.
     */
    @JobWorker(type = "create-fulfilment")
    public Map<String, Object> createFulfilment(
            @Variable(name = "customerRef") String customerRef,
            @Variable(name = "productId") String productId,
            @Variable(name = "quantity") Integer quantity,
            @Variable(name = "orderTotal") Double orderTotal,
            ActivatedJob job) {

        logger.info("Creating fulfilment for customer: {}, product: {}, quantity: {}, total: ${}",
                customerRef, productId, quantity, orderTotal);

        String fulfilmentId = "FUL-" + System.currentTimeMillis();

        Map<String, Object> variables = new HashMap<>();
        variables.put("fulfilmentId", fulfilmentId);

        logger.info("Fulfilment created successfully with ID: {}", fulfilmentId);

        return variables;
    }

    /**
     * Handles payment failure scenario.
     */
    @JobWorker(type = "handle-payment-failure")
    public Map<String, Object> handlePaymentFailure(
            @Variable(name = "customerRef") String customerRef,
            @Variable(name = "orderTotal") Double orderTotal,
            ActivatedJob job) {

        logger.warn("Payment failed for customer: {}, order total: ${}", customerRef, orderTotal);
        logger.info("Sending payment failure notification to customer: {}", customerRef);

        Map<String, Object> variables = new HashMap<>();
        variables.put("notificationSent", true);

        logger.info("Payment failure notification sent successfully");

        return variables;
    }

    /**
     * Handles out-of-stock scenario.
     */
    @JobWorker(type = "handle-out-of-stock")
    public Map<String, Object> handleOutOfStock(
            @Variable(name = "customerRef") String customerRef,
            @Variable(name = "productId") String productId,
            @Variable(name = "quantity") Integer quantity,
            ActivatedJob job) {

        logger.warn("Product out of stock - Customer: {}, Product: {}, Requested quantity: {}",
                customerRef, productId, quantity);
        logger.info("Sending out-of-stock notification to customer: {}", customerRef);

        Map<String, Object> variables = new HashMap<>();
        variables.put("notificationSent", true);

        logger.info("Out-of-stock notification sent successfully");

        return variables;
    }

    /**
     * Handles invalid order scenario.
     */
    @JobWorker(type = "handle-invalid-order")
    public Map<String, Object> handleInvalidOrder(
            @Variable(name = "customerRef") String customerRef,
            @Variable(name = "validationMessage") String validationMessage,
            ActivatedJob job) {

        logger.warn("Invalid order for customer: {} - Reason: {}", customerRef, validationMessage);
        logger.info("Sending invalid order notification to customer: {}", customerRef);

        Map<String, Object> variables = new HashMap<>();
        variables.put("notificationSent", true);

        logger.info("Invalid order notification sent successfully");

        return variables;
    }
}
