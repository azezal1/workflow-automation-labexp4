# Lab 2: E-Commerce Order Processing Worker

Spring Boot 3.x external job worker for Camunda 8 e-commerce order processing workflow.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Camunda 8 (Self-Managed or SaaS)
- Running Zeebe broker (for Self-Managed: default on localhost:26500)

## Project Structure

```
order-worker/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── camunda/
│       │           └── lab2/
│       │               ├── OrderWorkerApplication.java
│       │               └── workers/
│       │                   └── OrderValidationWorker.java
│       └── resources/
│           └── application.properties
```

## Configuration

### Self-Managed Setup (Default)

Edit `src/main/resources/application.properties`:

```properties
zeebe.client.broker.gateway-address=127.0.0.1:26500
zeebe.client.security.plaintext=true
```

### Camunda 8 SaaS Setup

Comment out the self-managed configuration and uncomment the cloud configuration:

```properties
zeebe.client.cloud.cluster-id=your-cluster-id
zeebe.client.cloud.client-id=your-client-id
zeebe.client.cloud.client-secret=your-client-secret
zeebe.client.cloud.region=bru-2
```

## Building the Application

```bash
mvn clean package
```

## Running the Worker

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/order-worker-1.0.0.jar
```

The worker will start on port **8082** and begin polling for jobs.

## Job Workers

This application implements five Zeebe job workers:

### 1. validate-order

**Type:** `validate-order`

**Input Variables:**
- `customerRef` (String): Customer reference identifier
- `productId` (String): Product identifier (P001, P002, P003, or others)
- `quantity` (Integer): Order quantity
- `deliveryPreference` (String): Delivery preference
- `paymentToken` (String): Payment token for mock service

**Output Variables:**
- `orderValid` (Boolean): Whether order passes validation
- `stockAvailable` (Boolean): Whether inventory is sufficient (quantity ≤ 100)
- `orderTotal` (Double): Calculated order total (quantity × unit price)
- `validationMessage` (String): Validation result message
- `customerCategory` (String): Derived category based on order total
  - "Premium": orderTotal > $500
  - "Regular": orderTotal > $100
  - "Basic": orderTotal ≤ $100

**Product Catalog:**
- P001: $29.99
- P002: $149.99
- P003: $499.99
- Default: $99.99

**Inventory Logic:**
- Stock available if quantity ≤ 100
- Stock unavailable if quantity > 100

### 2. create-fulfilment

**Type:** `create-fulfilment`

**Input Variables:**
- `customerRef` (String)
- `productId` (String)
- `quantity` (Integer)
- `orderTotal` (Double)

**Output Variables:**
- `fulfilmentId` (String): Generated fulfilment ID (format: FUL-{timestamp})

**Purpose:** Creates a fulfilment record for approved orders.

### 3. handle-payment-failure

**Type:** `handle-payment-failure`

**Input Variables:**
- `customerRef` (String)
- `orderTotal` (Double)

**Output Variables:**
- `notificationSent` (Boolean): Set to true after logging notification

**Purpose:** Handles payment failure scenarios and logs customer notification.

### 4. handle-out-of-stock

**Type:** `handle-out-of-stock`

**Input Variables:**
- `customerRef` (String)
- `productId` (String)
- `quantity` (Integer)

**Output Variables:**
- `notificationSent` (Boolean): Set to true after logging notification

**Purpose:** Handles out-of-stock scenarios and logs customer notification.

### 5. handle-invalid-order

**Type:** `handle-invalid-order`

**Input Variables:**
- `customerRef` (String)
- `validationMessage` (String)

**Output Variables:**
- `notificationSent` (Boolean): Set to true after logging notification

**Purpose:** Handles invalid order scenarios and logs customer notification.

## Health Check

The worker exposes actuator endpoints:

- Health: http://localhost:8082/actuator/health
- Info: http://localhost:8082/actuator/info

## Testing

### Test Scenario 1: Valid Order with Stock Available

```json
{
  "customerRef": "CUST-001",
  "productId": "P001",
  "quantity": 5,
  "deliveryPreference": "Standard",
  "paymentToken": "PAY-TOKEN-123"
}
```

**Expected:**
- orderValid: true
- stockAvailable: true
- orderTotal: 149.95
- customerCategory: "Regular"

### Test Scenario 2: High-Value Premium Order

```json
{
  "customerRef": "CUST-002",
  "productId": "P003",
  "quantity": 3,
  "deliveryPreference": "Express",
  "paymentToken": "PAY-TOKEN-456"
}
```

**Expected:**
- orderValid: true
- stockAvailable: true
- orderTotal: 1499.97
- customerCategory: "Premium"

### Test Scenario 3: Out of Stock

```json
{
  "customerRef": "CUST-003",
  "productId": "P002",
  "quantity": 150,
  "deliveryPreference": "Standard",
  "paymentToken": "PAY-TOKEN-789"
}
```

**Expected:**
- orderValid: true
- stockAvailable: false
- orderTotal: 22498.50
- customerCategory: "Premium"

### Test Scenario 4: Invalid Order (Missing Fields)

```json
{
  "customerRef": "",
  "productId": "P001",
  "quantity": 0,
  "deliveryPreference": "",
  "paymentToken": ""
}
```

**Expected:**
- orderValid: false
- stockAvailable: false
- orderTotal: 0.0
- validationMessage: "Customer reference is required. Quantity must be greater than zero. Delivery preference is required. Payment token is required."

## Troubleshooting

### Worker Not Connecting

1. Verify Zeebe broker is running:
   ```bash
   curl http://localhost:26500/ready
   ```

2. Check application.properties configuration

3. Review logs for connection errors

### Jobs Not Being Picked Up

1. Ensure job type in BPMN matches worker type exactly
2. Check worker logs for activation
3. Verify worker is polling (check actuator health endpoint)

## Integration with BPMN

In your BPMN model, create service tasks with the following job types:
- `validate-order`
- `create-fulfilment`
- `handle-payment-failure`
- `handle-out-of-stock`
- `handle-invalid-order`

Ensure input/output variable mappings match the documented variables above.

## Logs

The worker logs all processing steps at INFO level. Monitor logs to track:
- Order validation results
- Stock availability checks
- Order total calculations
- Customer category derivation
- Fulfilment creation
- Error handling

## Notes

- This worker uses synthetic test data only
- No real payment processing is performed
- Inventory checks are simulated based on quantity threshold
- All notifications are logged only (no actual email/SMS sent)
