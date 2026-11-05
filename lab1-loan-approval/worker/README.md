# Lab 1: Loan Approval Worker

Spring Boot 3.x external job worker for Camunda 8 Loan Approval workflow automation.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Camunda 8 cluster (local or cloud) running on:
  - gRPC endpoint: `localhost:26500`
  - REST endpoint: `http://localhost:8080`

## Project Structure

```
worker/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── camunda/
│       │           └── lab1/
│       │               ├── LoanWorkerApplication.java
│       │               └── workers/
│       │                   └── LoanValidationWorker.java
│       └── resources/
│           └── application.properties
└── README.md
```

## Job Workers

### 1. validate-applicant
**Type:** `validate-applicant`

**Input Variables:**
- `applicantId` (String) - Unique applicant identifier
- `applicantName` (String) - Full name of applicant
- `requestedAmount` (Double) - Loan amount requested
- `monthlyIncome` (Double) - Applicant's monthly income
- `employmentStatus` (String) - Current employment status

**Output Variables:**
- `validationPassed` (Boolean) - True if all validations pass
- `validationErrors` (String) - Comma-separated list of errors (null if valid)

**Validation Rules:**
- Applicant ID must not be blank
- Applicant name must not be blank
- Requested amount must be > 0 and <= 1,000,000
- Monthly income must be > 0
- Employment status must not be blank

### 2. send-notification
**Type:** `send-notification`

**Input Variables:**
- `applicantId` (String) - Unique applicant identifier
- `applicantName` (String) - Full name of applicant
- `decision` (String) - Loan decision (Approved/Rejected/Manual Review)
- `requestedAmount` (Double) - Loan amount requested

**Output Variables:**
- `notificationSent` (Boolean) - Always true
- `notificationTimestamp` (Long) - Unix timestamp
- `notificationMessage` (String) - Formatted notification message

## Build and Run

### 1. Build the project
```bash
cd worker
mvn clean package
```

### 2. Run the worker
```bash
mvn spring-boot:run
```

Or run the JAR:
```bash
java -jar target/loan-worker-1.0.0.jar
```

### 3. Verify the worker is running
Access the health endpoint:
```
http://localhost:8081/actuator/health
```

## Configuration

Edit `src/main/resources/application.properties` to change:

- **Zeebe broker address**: `zeebe.client.broker.grpcAddress`
- **REST API address**: `zeebe.client.broker.restAddress`
- **Worker threads**: `zeebe.client.worker.threads`
- **Server port**: `server.port`
- **Logging level**: `logging.level.com.camunda.lab1`

## Testing with Camunda 8

### Deploy BPMN Model
The BPMN process should include service tasks with types matching:
- `validate-applicant` - For applicant validation
- `send-notification` - For notification logging

### Start Process Instance with Variables
```json
{
  "applicantId": "APP001",
  "applicantName": "John Doe",
  "requestedAmount": 50000,
  "monthlyIncome": 5000,
  "employmentStatus": "Full-time"
}
```

### Test Scenarios

**Valid Application:**
```json
{
  "applicantId": "APP001",
  "applicantName": "Jane Smith",
  "requestedAmount": 75000,
  "monthlyIncome": 8000,
  "employmentStatus": "Employed"
}
```

**Invalid Application (Exceeds Max):**
```json
{
  "applicantId": "APP002",
  "applicantName": "Bob Wilson",
  "requestedAmount": 1500000,
  "monthlyIncome": 3000,
  "employmentStatus": "Self-employed"
}
```

**Invalid Application (Missing Data):**
```json
{
  "applicantId": "",
  "applicantName": "Alice Brown",
  "requestedAmount": -1000,
  "monthlyIncome": 0,
  "employmentStatus": ""
}
```

## Monitoring

### Worker Logs
The worker outputs detailed logs showing:
- Job activation
- Validation results
- Notification sending
- Variable values

### Actuator Endpoints
- Health: `http://localhost:8081/actuator/health`
- Info: `http://localhost:8081/actuator/info`

## Troubleshooting

### Worker not connecting to Zeebe
- Verify Zeebe broker is running
- Check `grpcAddress` in application.properties
- Ensure port 26500 is accessible

### Jobs not being picked up
- Verify job type matches BPMN service task type
- Check worker logs for errors
- Verify process is deployed and instance started

### Build errors
- Ensure Java 17 is installed: `java -version`
- Ensure Maven is installed: `mvn -version`
- Run `mvn clean install -U` to force update dependencies

## Dependencies

- **Spring Boot**: 3.2.5
- **Camunda Spring Boot Starter**: 8.5.0
- **Java**: 17

## License

Educational use only - Camunda 8 Practical Assignment
