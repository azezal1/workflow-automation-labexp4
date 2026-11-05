# Lab 2: E-Commerce Order Processing - Process Variables

This document describes all process variables used throughout the order processing workflow.

## Process Variables Dictionary

| Variable Name | Type | Direction | Description | Example Value | Set By |
|--------------|------|-----------|-------------|---------------|---------|
| `customerRef` | String | Input | Unique customer reference identifier | `"CUST-2024-001"` | Process Start |
| `productId` | String | Input | Product identifier from catalog | `"PROD-LAPTOP-5678"` | Process Start |
| `quantity` | Integer | Input | Number of units ordered | `2` | Process Start |
| `deliveryPreference` | String | Input | Delivery method preference | `"express"`, `"standard"`, `"pickup"` | Process Start |
| `paymentToken` | String | Input | Tokenized payment credential for mock service | `"tok_visa_4242_success"` | Process Start |
| `orderValid` | Boolean | Output | Indicates if order passed validation checks | `true` / `false` | Order Validation Worker |
| `stockAvailable` | Boolean | Output | Indicates if sufficient inventory exists | `true` / `false` | Order Validation Worker |
| `orderTotal` | Double | Output | Calculated total order amount in cents | `15999` (= $159.99) | Order Validation Worker |
| `customerCategory` | String | Output | Customer tier classification | `"premium"`, `"standard"`, `"new"` | Order Validation Worker |
| `orderPriority` | String | Output | Priority level determined by DMN table | `"high"`, `"medium"`, `"low"` | DMN Priority Decision |
| `paymentStatus` | String | Output | Payment processing outcome | `"completed"`, `"declined"`, `"failed"` | Payment REST Connector |
| `transactionId` | String | Output | Payment gateway transaction identifier | `"txn_1OkPQ82eZvKYlo2C8k7j9Xyz"` | Payment REST Connector |
| `fulfilmentId` | String | Output | Fulfilment tracking reference | `"FUL-20240115-001"` | Fulfilment Task |
| `verificationNotes` | String | Output | Notes added during manual review | `"Customer verified via phone"` | Manual Review User Task |
| `verificationApproved` | Boolean | Output | Manual review decision | `true` / `false` | Manual Review User Task |
| `validationMessage` | String | Output | Validation error or warning message | `"Invalid product ID"` | Order Validation Worker |
| `notificationSent` | Boolean | Output | Indicates customer notification sent | `true` / `false` | Notification Service Task |

## Variable Flow Through Process

### 1. Process Initialization
- **Inputs received**: `customerRef`, `productId`, `quantity`, `deliveryPreference`, `paymentToken`

### 2. Order Validation Worker
- **Reads**: `customerRef`, `productId`, `quantity`
- **Writes**: `orderValid`, `stockAvailable`, `orderTotal`, `customerCategory`, `validationMessage`

### 3. Priority Decision (DMN)
- **Reads**: `orderTotal`, `customerCategory`, `deliveryPreference`
- **Writes**: `orderPriority`

### 4. Stock Check Gateway
- **Routes based on**: `stockAvailable`
- **Path A** (stock available): Continue to payment
- **Path B** (insufficient stock): End process with notification

### 5. Payment Processing (REST Connector)
- **Reads**: `paymentToken`, `orderTotal`, `customerRef`
- **Writes**: `paymentStatus`, `transactionId`

### 6. Payment Outcome Gateway
- **Routes based on**: `paymentStatus`
- **Path A** (completed): Continue to fulfilment check
- **Path B** (declined/failed): End process with notification

### 7. High-Value Review Gateway
- **Routes based on**: `orderTotal` > 50000 (i.e., $500.00)
- **Path A** (high-value): Route to manual review
- **Path B** (standard): Continue to fulfilment

### 8. Manual Review User Task
- **Reads**: `customerRef`, `productId`, `quantity`, `orderTotal`, `orderPriority`
- **Writes**: `verificationNotes`, `verificationApproved`

### 9. Fulfilment Task
- **Reads**: `customerRef`, `productId`, `quantity`, `deliveryPreference`, `orderPriority`
- **Writes**: `fulfilmentId`, `notificationSent`

## Data Types and Validation Rules

| Variable | Validation Rule | Error Handling |
|----------|----------------|----------------|
| `customerRef` | Must match pattern `CUST-\d{4}-\d{3}` | Set `orderValid = false` |
| `productId` | Must exist in catalog | Set `orderValid = false`, populate `validationMessage` |
| `quantity` | Must be > 0 and <= 100 | Set `orderValid = false` |
| `orderTotal` | Calculated as (unit price × quantity) in cents | N/A |
| `paymentToken` | Must start with `tok_` prefix | Handled by payment gateway |
| `deliveryPreference` | Must be one of: `express`, `standard`, `pickup` | Default to `standard` if invalid |

## Notes

- All monetary values are stored in **cents** (integer) to avoid floating-point precision issues
- Boolean variables default to `false` if not explicitly set
- The `validationMessage` variable is only populated when validation fails
- Process variables are persisted and visible in Camunda Operate for inspection and debugging
- Sensitive payment data is tokenized; the `paymentToken` does not contain actual card details
