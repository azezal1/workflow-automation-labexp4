# Lab 3: Hospital Appointment Worker

Spring Boot 3.2.x external job worker for Camunda 8 hospital appointment and patient triage workflow.

## Configuration

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Camunda 8 cluster (SaaS or Self-Managed)

### Setup

1. **Configure Zeebe Connection**

   Edit `src/main/resources/application.properties` and set your Camunda 8 credentials:

   ```properties
   zeebe.client.cloud.cluster-id=your-cluster-id
   zeebe.client.cloud.client-id=your-client-id
   zeebe.client.cloud.client-secret=your-client-secret
   zeebe.client.cloud.region=bru-2
   ```

   Or use environment variables:
   ```bash
   export ZEEBE_CLUSTER_ID=your-cluster-id
   export ZEEBE_CLIENT_ID=your-client-id
   export ZEEBE_CLIENT_SECRET=your-client-secret
   ```

2. **Build the Application**

   ```bash
   mvn clean package
   ```

3. **Run the Worker**

   ```bash
   mvn spring-boot:run
   ```

   Or run the JAR directly:
   ```bash
   java -jar target/appointment-worker-1.0.0.jar
   ```

## Job Workers

The application implements four Zeebe job workers:

### 1. validate-registration
- **Type**: `validate-registration`
- **Input Variables**:
  - `patientRef` (String): Patient reference code
  - `patientName` (String): Patient full name
  - `appointmentPreference` (String): Preferred appointment date/time
  - `symptomsDescription` (String): Description of symptoms
  - `urgencyIndicators` (String): Urgency level (LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY)

- **Output Variables**:
  - `registrationValid` (Boolean): True if validation passes
  - `validationErrors` (String): Error messages if validation fails

- **Validation Rules**:
  - All fields are mandatory (not blank)
  - `patientRef` must match pattern `[A-Z]{2}[0-9]{6}` (e.g., "AB123456")
  - `urgencyIndicators` must be one of: LOW, MEDIUM, HIGH, CRITICAL, EMERGENCY

### 2. book-appointment
- **Type**: `book-appointment`
- **Input Variables**:
  - `patientRef` (String): Patient reference code
  - `slotId` (String): Selected appointment slot ID
  - `triageCategory` (String): Assigned triage category

- **Output Variables**:
  - `bookingStatus` (String): "CONFIRMED" if successful
  - `bookingConfirmation` (String): Booking confirmation code (format: "BOOK-{patientRef}-{timestamp}")

### 3. handle-registration-error
- **Type**: `handle-registration-error`
- **Input Variables**:
  - `patientRef` (String): Patient reference code
  - `validationErrors` (String): Error messages from validation

- **Output Variables**:
  - `notificationSent` (Boolean): True when error notification is sent

- **Purpose**: Logs errors and sends notification to patient about registration issues

### 4. send-triage-notification
- **Type**: `send-triage-notification`
- **Input Variables**:
  - `patientRef` (String): Patient reference code
  - `triageCategory` (String): Assigned triage category
  - `bookingStatus` (String): Current booking status

- **Output Variables**:
  - `notificationSent` (Boolean): True when triage notification is sent

- **Purpose**: Logs triage result and sends notification to patient

## Testing

### Valid Patient Reference Examples
- AB123456
- XY987654
- PQ555555

### Invalid Patient Reference Examples
- abc123456 (lowercase letters)
- AB12345 (too few digits)
- AB1234567 (too many digits)
- 12AB3456 (wrong format)

### Test Scenarios

1. **Valid Registration**
   ```json
   {
     "patientRef": "AB123456",
     "patientName": "John Doe",
     "appointmentPreference": "2024-12-15 10:00",
     "symptomsDescription": "Persistent headache for 3 days",
     "urgencyIndicators": "MEDIUM"
   }
   ```

2. **Invalid Patient Reference**
   ```json
   {
     "patientRef": "abc123",
     "patientName": "Jane Smith",
     "appointmentPreference": "2024-12-16 14:00",
     "symptomsDescription": "Chest pain",
     "urgencyIndicators": "HIGH"
   }
   ```

3. **Missing Required Field**
   ```json
   {
     "patientRef": "CD789012",
     "patientName": "",
     "appointmentPreference": "2024-12-17 09:00",
     "symptomsDescription": "Fever",
     "urgencyIndicators": "LOW"
   }
   ```

4. **Invalid Urgency Indicator**
   ```json
   {
     "patientRef": "EF345678",
     "patientName": "Bob Johnson",
     "appointmentPreference": "2024-12-18 11:00",
     "symptomsDescription": "Minor injury",
     "urgencyIndicators": "INVALID"
   }
   ```

## Monitoring

The worker logs all operations at DEBUG level. Monitor the console for:
- Job activation and completion
- Validation results
- Booking confirmations
- Error notifications

Health endpoint available at: `http://localhost:8083/actuator/health`

## Architecture

```
appointment-worker/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/camunda/lab3/
│       │       ├── AppointmentWorkerApplication.java
│       │       └── workers/
│       │           └── RegistrationWorker.java
│       └── resources/
│           └── application.properties
└── README.md
```

## Dependencies

- Spring Boot 3.2.5
- Camunda Spring Boot Starter 8.5.0
- Java 17

## Troubleshooting

### Connection Issues
- Verify Zeebe cluster credentials
- Check network connectivity to Camunda 8 Cloud
- Ensure correct cluster region is configured

### Job Not Activated
- Verify BPMN process uses correct job type names
- Check worker is running and connected
- Review Operate for process instance state

### Validation Failures
- Check input variable names match exactly
- Ensure variable types are correct
- Review worker logs for detailed error messages
