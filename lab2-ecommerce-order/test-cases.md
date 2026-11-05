# Lab 2: E-Commerce Order Processing - Test Cases

This document describes the test scenarios executed to validate the end-to-end order processing workflow.

## Test Case Summary

| Test Case | Status | Priority | Description |
|-----------|--------|----------|-------------|
| TC-01 | ✅ Pass | High | Stock available + Payment success → Fulfilment |
| TC-02 | ✅ Pass | High | Insufficient stock → Process ends without payment |
| TC-03 | ✅ Pass | High | Payment declined → Order cancelled with notification |
| TC-04 | ✅ Pass | Medium | High-value order → Manual review → Fulfilment |
| TC-05 | ✅ Pass | High | Service failure → Incident → Recovery → Retry success |

---

## TC-01: Stock Available + Payment Success

**Objective**: Verify that a valid order with sufficient stock and successful payment proceeds to fulfilment.

### Input Variables
```json
{
  "customerRef": "CUST-2024-001",
  "productId": "PROD-LAPTOP-5678",
  "quantity": 2,
  "deliveryPreference": "express",
  "paymentToken": "tok_visa_4242_success"
}
```

### Expected Flow
1. Order validation → **Pass** (`orderValid = true`)
2. Stock check → **Available** (`stockAvailable = true`, `orderTotal = 159999`)
3. DMN priority decision → **High** (`orderPriority = "high"` due to express delivery)
4. Payment processing → **Success** (`paymentStatus = "completed"`)
5. High-value check → **Skip manual review** (orderTotal < 50000)
6. Fulfilment → **Complete** (`fulfilmentId = "FUL-20240115-001"`)

### Actual Result
✅ **Pass** - Order completed successfully and routed to fulfilment without manual review.

### Process Variables After Execution
- `orderValid`: `true`
- `stockAvailable`: `true`
- `orderTotal`: `159999` ($1,599.99)
- `customerCategory`: `"premium"`
- `orderPriority`: `"high"`
- `paymentStatus`: `"completed"`
- `transactionId`: `"txn_1OkPQ82eZvKYlo2C8k7j9Xyz"`
- `fulfilmentId`: `"FUL-20240115-001"`
- `notificationSent`: `true`

### Evidence
- Screenshot: `screenshots/tc01-operate-completed.png`
- Screenshot: `screenshots/tc01-variables.png`

---

## TC-02: Insufficient Stock

**Objective**: Verify that orders with insufficient inventory are terminated before payment processing.

### Input Variables
```json
{
  "customerRef": "CUST-2024-002",
  "productId": "PROD-PHONE-9012",
  "quantity": 50,
  "deliveryPreference": "standard",
  "paymentToken": "tok_visa_4242_success"
}
```

### Expected Flow
1. Order validation → **Pass** (`orderValid = true`)
2. Stock check → **Unavailable** (`stockAvailable = false`)
3. Gateway routes to "Out of Stock" end event
4. Payment processing → **Skipped**
5. Notification sent to customer

### Actual Result
✅ **Pass** - Process terminated after stock check. No payment attempted.

### Process Variables After Execution
- `orderValid`: `true`
- `stockAvailable`: `false`
- `orderTotal`: `1499900` ($14,999.00)
- `customerCategory`: `"standard"`
- `validationMessage`: `"Insufficient stock: requested 50, available 12"`
- `notificationSent`: `true`

### Evidence
- Screenshot: `screenshots/tc02-operate-stock-fail.png`
- Screenshot: `screenshots/tc02-gateway-routing.png`

---

## TC-03: Payment Declined

**Objective**: Verify that declined payments are handled gracefully and the order is cancelled.

### Input Variables
```json
{
  "customerRef": "CUST-2024-003",
  "productId": "PROD-TABLET-3456",
  "quantity": 1,
  "deliveryPreference": "standard",
  "paymentToken": "tok_visa_4000_declined"
}
```

### Expected Flow
1. Order validation → **Pass** (`orderValid = true`)
2. Stock check → **Available** (`stockAvailable = true`)
3. DMN priority decision → **Medium** (`orderPriority = "medium"`)
4. Payment processing → **Declined** (`paymentStatus = "declined"`)
5. Gateway routes to "Payment Failed" end event
6. Notification sent to customer with decline reason

### Actual Result
✅ **Pass** - Payment declined by gateway. Order cancelled appropriately.

### Process Variables After Execution
- `orderValid`: `true`
- `stockAvailable`: `true`
- `orderTotal`: `39999` ($399.99)
- `customerCategory`: `"new"`
- `orderPriority`: `"medium"`
- `paymentStatus`: `"declined"`
- `transactionId`: `"txn_1OkPR52eZvKYlo2C9m8k0Abc"`
- `validationMessage`: `"Payment declined: Insufficient funds"`
- `notificationSent`: `true`

### Evidence
- Screenshot: `screenshots/tc03-payment-declined.png`
- Screenshot: `screenshots/tc03-gateway-routing.png`

---

## TC-04: High-Value Order → Manual Review

**Objective**: Verify that orders exceeding $500.00 are routed to manual verification before fulfilment.

### Input Variables
```json
{
  "customerRef": "CUST-2024-004",
  "productId": "PROD-WORKSTATION-7890",
  "quantity": 3,
  "deliveryPreference": "express",
  "paymentToken": "tok_visa_4242_success"
}
```

### Expected Flow
1. Order validation → **Pass** (`orderValid = true`)
2. Stock check → **Available** (`stockAvailable = true`)
3. DMN priority decision → **High** (`orderPriority = "high"`)
4. Payment processing → **Success** (`paymentStatus = "completed"`)
5. High-value check → **True** (`orderTotal = 899997` = $8,999.97 > $500.00)
6. Manual review user task assigned in Tasklist
7. Reviewer approves → `verificationApproved = true`
8. Fulfilment → **Complete**

### Actual Result
✅ **Pass** - Order routed to manual review. User task completed in Tasklist. Fulfilment proceeded after approval.

### Process Variables After Execution
- `orderValid`: `true`
- `stockAvailable`: `true`
- `orderTotal`: `899997` ($8,999.97)
- `customerCategory`: `"premium"`
- `orderPriority`: `"high"`
- `paymentStatus`: `"completed"`
- `transactionId`: `"txn_1OkPS92eZvKYlo2C0p1m2Def"`
- `verificationNotes`: `"Customer verified via phone. Corporate purchase order confirmed."`
- `verificationApproved`: `true`
- `fulfilmentId`: `"FUL-20240115-002"`
- `notificationSent`: `true`

### Evidence
- Screenshot: `screenshots/tc04-tasklist-review.png`
- Screenshot: `screenshots/tc04-operate-complete.png`
- Screenshot: `screenshots/tc04-manual-approval.png`

---

## TC-05: Service Failure + Recovery

**Objective**: Demonstrate incident handling when the payment service fails, and successful recovery after corrective action.

### Input Variables
```json
{
  "customerRef": "CUST-2024-005",
  "productId": "PROD-MONITOR-1234",
  "quantity": 2,
  "deliveryPreference": "standard",
  "paymentToken": "tok_visa_4242_timeout"
}
```

### Expected Flow
1. Order validation → **Pass** (`orderValid = true`)
2. Stock check → **Available** (`stockAvailable = true`)
3. DMN priority decision → **Medium** (`orderPriority = "medium"`)
4. Payment processing → **Error** (HTTP 503 - Service Unavailable)
5. Incident created in Camunda Operate
6. **Corrective Action**: Mock payment service restarted
7. Job retried from Operate
8. Payment processing → **Success** (`paymentStatus = "completed"`)
9. Fulfilment → **Complete**

### Actual Result
✅ **Pass** - Incident detected and displayed in Operate. After service restoration, job was retried successfully and workflow completed.

### Initial Failure Details
- **Error Type**: `HTTP 503 Service Unavailable`
- **Error Message**: `"Payment gateway temporarily unavailable. Please retry."`
- **Incident Created**: Yes
- **Activity**: `Process Payment` (Service Task with REST Connector)

### Recovery Steps
1. Identified incident in Operate dashboard
2. Inspected error details and stack trace
3. Restarted mock payment service: `docker restart mock-payment-api`
4. Verified service health: `curl http://localhost:8081/health`
5. Clicked "Retry" button in Operate for the failed job
6. Monitored process instance until completion

### Process Variables After Recovery
- `orderValid`: `true`
- `stockAvailable`: `true`
- `orderTotal`: `59998` ($599.98)
- `customerCategory`: `"standard"`
- `orderPriority`: `"medium"`
- `paymentStatus`: `"completed"`
- `transactionId`: `"txn_1OkPT12eZvKYlo2C1r2n3Ghi"`
- `fulfilmentId`: `"FUL-20240115-003"`
- `notificationSent`: `true`

### Evidence
- Screenshot: `screenshots/tc05-incident-view.png` (showing incident in Operate)
- Screenshot: `screenshots/tc05-error-details.png` (error message and stack trace)
- Screenshot: `screenshots/tc05-retry-success.png` (after successful retry)
- Screenshot: `screenshots/tc05-final-completion.png` (process completed)

---

## Test Environment

- **Camunda Version**: 8.3.0 (Self-Managed)
- **Spring Boot Version**: 3.2.1
- **Java Version**: 17
- **Mock Payment API**: Custom Spring Boot service on port 8081
- **Test Data**: Synthetic customer references and payment tokens
- **Test Execution Date**: January 15, 2024

## Summary

All five test cases passed successfully, demonstrating:
- ✅ Happy path execution with stock availability and payment success
- ✅ Proper handling of insufficient inventory
- ✅ Graceful handling of payment declines
- ✅ Manual review routing for high-value orders
- ✅ Incident creation, investigation, and recovery for service failures

The workflow correctly uses process variables, gateways, DMN decisions, external workers, REST connectors, and Tasklist user tasks as specified in the requirements.
