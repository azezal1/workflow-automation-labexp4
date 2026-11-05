# LAB 1 Implementation Report: End-to-End Loan Approval Automation

**Course:** PBJ-2 Practical Assignment  
**Lab:** Lab 1 - Loan Approval Workflow  
**Platform:** Camunda 8  
**Date:** January 2024

---

## 1. Overview & Architecture

### 1.1 Purpose
This implementation demonstrates a complete loan approval workflow using Camunda 8, integrating BPMN process orchestration, DMN-based decision automation, external job workers, REST service integration, and human task management. The system accepts loan applications, validates applicant information, assesses creditworthiness, applies business rules, and routes applications to appropriate outcomes.

### 1.2 Architecture Components

The solution consists of five primary components:

1. **BPMN Process Model** (`loan-approval-process.bpmn`) - Orchestrates the end-to-end workflow
2. **DMN Decision Table** (`loan-decision.dmn`) - Applies business rules for approval/rejection/review routing
3. **Spring Boot Worker Application** - Executes validation logic as external job worker
4. **Credit Assessment REST API** - Simulated external service for credit scoring
5. **Camunda 8 Platform** - Process engine, Tasklist for human tasks, Operate for monitoring

### 1.3 Technology Stack

- **Process Engine:** Camunda 8 (Zeebe)
- **Modeling:** Camunda Modeler 5.x
- **Worker Runtime:** Java 17, Spring Boot 3.2.x
- **Worker Client:** Spring Zeebe 8.5.x
- **REST Integration:** Camunda HTTP Connector
- **Mock API:** Spring Boot REST Controller
- **Build Tool:** Maven 3.9.x

---

## 2. BPMN Process Flow Description

### 2.1 Process Overview

The loan approval process (`loan-approval-process`) implements a sequential workflow with decision points, external service calls, and conditional routing.

### 2.2 Process Activities

#### Start Event: "Loan Application Submitted"
- **Type:** None Start Event
- **Inputs:** applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus
- **Trigger:** API call or process start form

#### Service Task: "Validate Applicant Info"
- **Type:** External Job Worker Task
- **Task Type:** `validate-applicant`
- **Implementation:** Java worker validates required fields, calculates debt-to-income ratio
- **Outputs:** validationPassed (boolean), validationErrors (string), debtToIncomeRatio (double)
- **Error Handling:** Worker retries on failure; incidents created after 3 failed attempts

#### Exclusive Gateway: "Validation Passed?"
- **Type:** XOR Gateway
- **Condition:** `validationPassed == true`
- **Routes:**
  - **True:** Continue to Credit Assessment
  - **False:** End process with validation failure

#### Service Task: "Assess Credit"
- **Type:** HTTP REST Connector
- **Method:** POST
- **Endpoint:** `http://localhost:8081/api/credit/assess`
- **Input Mapping:** Maps applicantId, applicantName, monthlyIncome, requestedAmount, employmentStatus to request body
- **Output Mapping:** Extracts creditScore, creditStatus, assessmentDate, recommendation from response
- **Timeout:** 10 seconds
- **Retries:** Configured with exponential backoff

#### Business Rule Task: "Loan Decision"
- **Type:** DMN Decision Task
- **Decision ID:** `loan-decision`
- **Inputs:** monthlyIncome, requestedAmount, creditScore, employmentStatus, debtToIncomeRatio
- **Output:** loanDecision (string: "Approved" | "Rejected" | "Manual Review")

#### Exclusive Gateway: "Route Decision"
- **Type:** XOR Gateway
- **Conditions:**
  - `loanDecision == "Approved"` → Approval End Event
  - `loanDecision == "Rejected"` → Rejection End Event
  - `loanDecision == "Manual Review"` → Manual Review User Task

#### User Task: "Manual Review Required"
- **Type:** User Task
- **Assignment:** Candidate group: `loan-reviewers`
- **Form Fields:**
  - Display: applicantName, requestedAmount, monthlyIncome, creditScore, validationErrors
  - Input: reviewNotes (text area)
- **Output:** reviewNotes (string)
- **Tool:** Accessible via Camunda Tasklist

#### End Events
- **Loan Approved:** Process completes with loanDecision = "Approved"
- **Loan Rejected:** Process completes with loanDecision = "Rejected"
- **Review Completed:** Process completes after manual review with reviewNotes recorded

### 2.3 Process Flow Diagram Summary

```
[Start] → [Validate Applicant] → [Valid?] 
                                     ↓ Yes
                              [Assess Credit] → [Loan Decision DMN]
                                                      ↓
                                               [Route Decision]
                                    ↓                ↓              ↓
                              [Approved]        [Rejected]    [Manual Review]
                                  ↓                ↓              ↓
                               [End]            [End]       [User Task] → [End]
```

---

## 3. DMN Decision Table Explanation

### 3.1 Decision Structure

**Decision ID:** `loan-decision`  
**Decision Name:** Loan Approval Decision  
**Hit Policy:** First (F) - Returns the first matching rule

### 3.2 Input Columns

| Input | Label | Type | Description |
|-------|-------|------|-------------|
| `monthlyIncome` | Monthly Income | number | Applicant's monthly income in currency units |
| `requestedAmount` | Requested Amount | number | Loan amount requested |
| `creditScore` | Credit Score | number | Credit score from assessment (300-850) |
| `employmentStatus` | Employment Status | string | Employment type: FULL_TIME, PART_TIME, SELF_EMPLOYED, etc. |
| `debtToIncomeRatio` | Debt-to-Income Ratio | number | Calculated ratio as percentage |

### 3.3 Output Column

| Output | Label | Type | Values |
|--------|-------|------|--------|
| `loanDecision` | Loan Decision | string | "Approved", "Rejected", "Manual Review" |

### 3.4 Decision Rules (Educational Scenario)

| Rule | Credit Score | Debt-to-Income Ratio | Monthly Income | Employment Status | Loan Decision | Explanation |
|------|--------------|---------------------|----------------|-------------------|---------------|-------------|
| 1 | ≥ 750 | < 30% | ≥ 3000 | FULL_TIME | **Approved** | Excellent credit, low debt ratio, stable income |
| 2 | ≥ 700 | < 35% | ≥ 2500 | FULL_TIME, SELF_EMPLOYED | **Approved** | Good credit with acceptable debt ratio |
| 3 | < 600 | - | - | - | **Rejected** | Poor credit score regardless of other factors |
| 4 | - | ≥ 50% | - | - | **Rejected** | Debt-to-income ratio too high |
| 5 | - | - | < 2000 | - | **Rejected** | Insufficient income |
| 6 | - | - | - | UNEMPLOYED | **Rejected** | No employment |
| 7 | 600-699 | 35-50% | ≥ 2500 | FULL_TIME, PART_TIME | **Manual Review** | Borderline credit and debt ratio |
| 8 | ≥ 650 | < 40% | ≥ 2000 | SELF_EMPLOYED | **Manual Review** | Self-employed requires additional verification |
| 9 | - | - | - | PART_TIME, RETIRED | **Manual Review** | Non-standard employment requires review |
| 10 | - | - | - | - | **Manual Review** | Default catch-all for cases not matching above |

### 3.5 Decision Logic Notes

- Rules are evaluated in order (First hit policy)
- Stricter rejection criteria are evaluated before approval criteria
- Manual Review serves as a catch-all for borderline or complex cases
- Self-employed applicants always route to manual review for income verification
- The decision table complements but does not replace the validation worker

---

## 4. External Job Worker Implementation

### 4.1 Worker Architecture

The validation worker is implemented as a Spring Boot application using the Spring Zeebe client library. It subscribes to jobs of type `validate-applicant` and executes synchronous validation logic.

### 4.2 Worker Configuration

**Application Properties:**
```properties
zeebe.client.broker.gateway-address=127.0.0.1:26500
zeebe.client.security.plaintext=true
zeebe.client.worker.threads=2
zeebe.client.worker.default-name=loan-validation-worker
zeebe.client.worker.default-type=validate-applicant
```

### 4.3 Worker Implementation

**Key Components:**
- `@ZeebeWorker` annotation for job subscription
- Input variable extraction using `@Variable` annotations
- Validation logic execution
- Output variable mapping using `ActivatedJob.complete()`

**Validation Logic:**
1. Check for null or empty required fields (applicantId, applicantName, requestedAmount, monthlyIncome)
2. Validate data types and ranges (requestedAmount > 0, monthlyIncome > 0)
3. Validate employmentStatus against allowed enum values
4. Calculate debt-to-income ratio: `(requestedAmount / 12) / monthlyIncome * 100`
5. Return validationPassed flag and error list

**Sample Worker Code Structure:**
```java
@ZeebeWorker(type = "validate-applicant", name = "applicant-validation-worker")
public void validateApplicant(
    @Variable String applicantId,
    @Variable String applicantName,
    @Variable Double requestedAmount,
    @Variable Double monthlyIncome,
    @Variable String employmentStatus,
    final JobClient client,
    final ActivatedJob job
) {
    List<String> errors = new ArrayList<>();
    
    // Validation logic
    if (applicantId == null || applicantId.trim().isEmpty()) {
        errors.add("Applicant ID is required");
    }
    // ... additional validations
    
    double debtToIncomeRatio = (requestedAmount / 12.0) / monthlyIncome * 100;
    boolean passed = errors.isEmpty();
    
    // Complete job with output variables
    Map<String, Object> outputVariables = new HashMap<>();
    outputVariables.put("validationPassed", passed);
    outputVariables.put("validationErrors", String.join(", ", errors));
    outputVariables.put("debtToIncomeRatio", debtToIncomeRatio);
    
    client.newCompleteCommand(job.getKey())
          .variables(outputVariables)
          .send();
}
```

### 4.4 Error Handling

- **Transient Errors:** Worker throws exception, Zeebe retries with exponential backoff
- **Permanent Errors:** Worker completes job with validationPassed = false and error details
- **Incident Creation:** After 3 failed retry attempts, Zeebe creates an incident visible in Operate

### 4.5 Worker Deployment

The worker runs as a standalone Spring Boot application:
```bash
mvn clean package
java -jar target/loan-approval-worker-1.0.0.jar
```

Worker automatically connects to Zeebe gateway and begins polling for jobs.

---

## 5. REST Connector Configuration

### 5.1 Connector Overview

The Credit Assessment task uses Camunda's HTTP REST Connector to invoke an external credit scoring API. The connector is configured directly in the BPMN model using the Camunda Modeler.

### 5.2 Connector Configuration (BPMN Properties)

**Task Type:** `io.camunda:http-json:1`

**Configuration Parameters:**

```json
{
  "method": "POST",
  "url": "http://localhost:8081/api/credit/assess",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "applicantId": "=applicantId",
    "applicantName": "=applicantName",
    "monthlyIncome": "=monthlyIncome",
    "requestedAmount": "=requestedAmount",
    "employmentStatus": "=employmentStatus"
  },
  "connectionTimeoutInSeconds": 10,
  "readTimeoutInSeconds": 10
}
```

### 5.3 Input/Output Mapping

**Input Mapping (Process Variables → Request Body):**
- Uses FEEL expressions (`=variableName`) to map process variables to JSON request
- All applicant context is passed to the credit service

**Output Mapping (Response Body → Process Variables):**
```json
{
  "creditScore": "=response.body.creditScore",
  "creditStatus": "=response.body.creditStatus",
  "assessmentDate": "=response.body.assessmentDate",
  "recommendation": "=response.body.recommendation"
}
```

### 5.4 Mock Credit API

**Implementation:** Spring Boot REST Controller

**Endpoint:** `POST /api/credit/assess`

**Logic:**
- Accepts applicant details
- Calculates synthetic credit score based on income and employment
- Returns credit assessment response

**Sample Response:**
```json
{
  "applicantId": "APP-2024-001",
  "creditScore": 780,
  "creditStatus": "Good",
  "assessmentDate": "2024-01-15T10:30:00Z",
  "recommendation": "APPROVE"
}
```

### 5.5 Error Scenarios

- **HTTP 503:** Service unavailable - creates incident
- **HTTP 500:** Internal server error - triggers retry then incident
- **Timeout:** Connection/read timeout - triggers retry then incident
- **Network Error:** Connection refused - creates incident

All error scenarios are recoverable through Operate's retry mechanism after resolving the underlying issue.

---

## 6. Process Variable Flow

### 6.1 Variable Lifecycle

Process variables flow through the workflow in three phases:

**Phase 1: Input Collection (Start Event)**
- User provides: applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus
- Variables stored at process instance scope

**Phase 2: Enrichment (Service Tasks)**
- Validation worker adds: validationPassed, validationErrors, debtToIncomeRatio
- Credit API adds: creditScore, creditStatus, assessmentDate, recommendation
- DMN decision adds: loanDecision

**Phase 3: Human Review (User Task, conditional)**
- Reviewer adds: reviewNotes
- All previous variables available for display in task form

### 6.2 Variable Usage by Activity

See `process-variables.md` for complete variable dictionary.

**Key Variable Dependencies:**
- DMN decision requires: monthlyIncome, requestedAmount, creditScore, employmentStatus, debtToIncomeRatio
- Manual review displays: all input variables + creditScore + validationErrors
- End events carry: loanDecision + optional reviewNotes

### 6.3 Variable Scope

All variables are process instance variables with global scope, persisted throughout the process lifecycle and accessible in Operate's variable inspector.

---

## 7. Test Results Summary

### 7.1 Test Execution

Five comprehensive test scenarios were executed covering:
1. ✅ Successful approval path (high credit, low debt ratio)
2. ✅ Automatic rejection path (poor credit, high debt ratio)
3. ✅ Manual review path (borderline case, self-employed)
4. ✅ Credit API failure and incident creation (HTTP 503)
5. ✅ Worker failure, incident diagnosis, code correction, and successful retry

**Success Rate:** 100% (5/5 test cases passed)

### 7.2 Key Findings

**Normal Flow Performance:**
- Approval path: 2-3 seconds end-to-end
- Rejection path: 2-3 seconds end-to-end
- Manual review path: Completes immediately to user task, then waits for human input

**Error Handling:**
- REST Connector failures properly create incidents with descriptive error messages
- Worker exceptions are captured with full stack traces in Operate
- Retry mechanism works correctly after fixing underlying issues
- No data loss during incident recovery

**User Experience:**
- Tasklist displays review tasks with all relevant applicant information
- Variable inspector in Operate provides complete visibility into process state
- Incident resolution workflow is straightforward for operators

### 7.3 Test Evidence

Refer to:
- `test-cases.md` - Detailed test case specifications and results
- `screenshots/` directory - Tasklist and Operate screenshots demonstrating each scenario

---

## 8. Incident Recovery Procedure

### 8.1 Incident Detection

Incidents appear in Camunda Operate when:
- A job worker throws an exception and exhausts retries (default: 3 attempts)
- A REST Connector receives an error response or times out
- A BPMN error is thrown without a boundary event handler

### 8.2 Incident Diagnosis Steps

1. **Open Camunda Operate** and navigate to Instances view
2. **Filter by "Incidents"** to show only instances with active incidents
3. **Select the affected process instance**
4. **Navigate to the incident activity** (highlighted in red on the BPMN diagram)
5. **Click "View Incident Details"** to see:
   - Error message
   - Stack trace (for worker exceptions)
   - Failed job attempt history
   - Current variable state

### 8.3 Common Incident Scenarios

**Scenario 1: Worker Exception**
- **Symptom:** "validate-applicant" job failed with exception
- **Diagnosis:** Check stack trace in incident details, review worker logs
- **Resolution:** Fix worker code, redeploy worker, click "Retry" in Operate
- **Prevention:** Add comprehensive error handling and input validation in worker

**Scenario 2: REST API Unavailable**
- **Symptom:** "Assess Credit" task failed with HTTP 503 or connection timeout
- **Diagnosis:** Verify API endpoint is reachable, check API logs
- **Resolution:** Start/fix the credit API service, click "Retry" in Operate
- **Prevention:** Implement health checks, circuit breakers, and fallback mechanisms

**Scenario 3: DMN Evaluation Error**
- **Symptom:** Business rule task failed with "No matching rule"
- **Diagnosis:** Review input variables in Operate, verify DMN decision table has catch-all rule
- **Resolution:** Update DMN table to handle edge case, redeploy decision definition, retry job
- **Prevention:** Always include a default rule in DMN tables

### 8.4 Recovery Actions

**From Operate:**
1. **Retry Job:** Attempts the failed task again with existing variables (most common)
2. **Update Variables:** Modify process variables before retrying (for data correction)
3. **Cancel Process Instance:** Terminates the instance (for unrecoverable errors)

**Best Practice Recovery Flow:**
1. Document the incident (screenshot, error message)
2. Identify root cause
3. Apply fix (code, configuration, external service)
4. Test fix in isolation if possible
5. Retry job from Operate
6. Verify process continues successfully
7. Monitor for recurrence

### 8.5 Incident Prevention

- Comprehensive input validation in workers
- Defensive null checks and type validation
- Proper exception handling and logging
- Health checks for external services
- Timeout configuration for REST calls
- DMN catch-all rules for edge cases
- Thorough testing before production deployment

---

## 9. Limitations & Assumptions

### 9.1 Implementation Limitations

**Synthetic Data:**
- Credit API generates simulated scores based on simplified logic
- No actual credit bureau integration
- Test data does not reflect real-world distribution of credit profiles

**Simplified Business Rules:**
- DMN decision table uses educational rules, not actual lending criteria
- Does not consider credit history, existing debts, collateral, or loan purpose
- Debt-to-income calculation is simplified (monthly payment estimate only)

**Security:**
- No authentication or authorization implemented
- Process variables contain sensitive applicant data without encryption
- REST API calls use HTTP instead of HTTPS
- No audit logging of data access

**Scalability:**
- Single worker instance (no horizontal scaling demonstrated)
- Mock API is not production-grade
- No load testing performed

**Error Handling:**
- Limited retry strategies (uses Zeebe defaults)
- No compensation logic for partial failures
- No notification system for incidents

### 9.2 Architectural Assumptions

**Infrastructure:**
- Camunda 8 Self-Managed deployment on local development machine
- Worker and mock API run on localhost
- No container orchestration (Docker/Kubernetes)
- Single-node Zeebe cluster

**Process Assumptions:**
- Each application is processed independently (no batch processing)
- Manual review is performed within a reasonable time (no SLA enforcement)
- Approved loans are processed by downstream systems (out of scope)
- Rejected applicants are not automatically notified (out of scope)

**Data Assumptions:**
- Applicant information is provided complete and accurate
- Employment status is current and verified
- Monthly income is net income after taxes
- Requested amount is in the same currency as income

### 9.3 Known Issues

**Current Implementation:**
- Worker application must be manually started before process instances are created
- Credit API mock does not persist data or maintain state
- No graceful degradation if external services are unavailable
- Process variables are not validated at start event (relies on worker validation)

**Future Improvements:**
- Add input validation at process start using form constraints
- Implement proper error boundary events for better error handling
- Add compensation logic for multi-step transactions
- Integrate with real identity verification and credit bureau APIs
- Add email notification for application status updates
- Implement role-based access control for Tasklist
- Add process instance correlation for application updates
- Create operational dashboards for monitoring application volume and approval rates

### 9.4 Educational Context

This implementation is designed for educational purposes to demonstrate Camunda 8 capabilities including:
- BPMN 2.0 process modeling
- DMN 1.3 decision automation
- External job worker patterns
- REST service integration
- Human task management
- Process monitoring and incident recovery

**Not suitable for production use** without significant enhancements to security, error handling, scalability, compliance, and business logic.

---

## 10. Conclusion

This Lab 1 implementation successfully demonstrates end-to-end loan approval automation using Camunda 8. The solution integrates BPMN orchestration, DMN decision tables, external Java workers, REST service calls, and human review tasks into a cohesive workflow.

All required test scenarios passed, including normal approval/rejection paths, manual review routing, and incident recovery procedures. The implementation provides a solid foundation for understanding Camunda 8's core capabilities and patterns for workflow automation.

**Key Achievements:**
- ✅ Complete BPMN process with multiple paths
- ✅ DMN decision table with comprehensive rules
- ✅ Functional Spring Boot job worker
- ✅ REST Connector integration with mock API
- ✅ Process variable flow across all activities
- ✅ Manual review via Tasklist
- ✅ Incident handling and recovery in Operate
- ✅ Comprehensive test coverage

**Learning Outcomes:**
- Understanding of BPMN service tasks, gateways, and events
- DMN decision table design and evaluation
- External worker pattern implementation
- REST Connector configuration and error handling
- Process monitoring and operational troubleshooting
- Incident diagnosis and recovery procedures

The documentation, test cases, and implementation artifacts provide a complete reference for Camunda 8 workflow development and serve as a foundation for more complex automation scenarios in Labs 2 and 3.

---

**End of Lab 1 Report**
