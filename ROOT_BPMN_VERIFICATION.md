# Root BPMN Files Verification (lab1.bpmn, lab2.bpmn, lab3.bpmn)

## Files to Open in Camunda Modeler:
- `lab1.bpmn` (Loan Approval)
- `lab2.bpmn` (E-Commerce Order)
- `lab3.bpmn` (Hospital Appointment)

---

## ✅ LAB1.BPMN - Loan Approval Process

### Requirement 3: Java/Spring Boot External Job Worker
**Status: ✅ IMPLEMENTED**

**Service Tasks with Job Types:**
1. **Task_ValidateApplicant** - "Validate Applicant Information"
   - Job Type: `validate-applicant`
   - Purpose: Validates applicant data
   - Process Variables:
     - Input: applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus
     - Output: validationPassed, validationErrors

2. **Task_ProcessApproval** - "Process Loan Approval"
   - Job Type: `process-approval`
   - Purpose: Processes approved loans

3. **Task_ProcessRejection** - "Process Loan Rejection"
   - Job Type: `process-rejection`
   - Purpose: Processes rejected loans

**Worker Location:** `lab1-loan-approval/worker/`

---

### Requirement 4: REST Connector for Credit Assessment API
**Status: ✅ IMPLEMENTED**

**REST Connector Task:**
- **Task ID:** Task_CreditAssessment
- **Task Name:** "Credit Assessment API"
- **Connector Type:** `io.camunda:http-json:1` (Official Camunda HTTP JSON Connector)
- **Template:** `io.camunda.connectors.HttpJson.v2`
- **Configuration:**
  - Method: GET
  - URL: (Configurable via process variables)
  - Authentication: noAuth
  - Timeouts: 20s connection, 20s read
  - Retries: 3

**When you open in Camunda Modeler:**
- You'll see a service task with an "R" icon (REST Connector)
- Properties panel shows "HTTP Connector" template
- Can configure URL, method, headers, authentication

---

### Requirement 5: Process Variables for Data Exchange
**Status: ✅ IMPLEMENTED**

**Variable Flow:**
1. **Start Event** → Initial applicant data
2. **Task_ValidateApplicant** (Worker)
   - IN: applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus
   - OUT: validationPassed, validationErrors
3. **Task_CreditAssessment** (REST API)
   - IN: applicantId, requestedAmount, monthlyIncome
   - OUT: creditScore, creditStatus, myResponseBody
4. **Task_EvaluateApplication** (DMN)
   - IN: creditScore, monthlyIncome, requestedAmount, creditStatus
   - OUT: loanDecision
5. **Gateway_Decision** → Routes based on `loanDecision` variable
   - "Approved" → Process Approval
   - "Rejected" → Process Rejection
   - "Manual Review" → User Task
6. **Task_ManualReview** (User Task)
   - IN: All previous variables for review
   - OUT: manualDecision
7. **End Events** → Loan Approved or Loan Rejected

**Variables Passed Between Activities:**
- ✅ Applicant details passed to validation worker
- ✅ Validation results passed to credit API
- ✅ Credit assessment results passed to DMN
- ✅ Decision outcomes used for gateway routing

---

## ✅ LAB2.BPMN - E-Commerce Order Processing

### Requirement 3: Java/Spring Boot External Job Worker
**Status: ✅ IMPLEMENTED**

**Service Tasks with Job Types:**
1. **Task_ValidateOrder** - "Validate Order and Check Inventory"
   - Job Type: `validate-order`
   - Purpose: Validates order and checks stock availability
   - Process Variables:
     - Input: customerRef, productId, quantity, deliveryPreference, customerCategory
     - Output: orderValid, stockAvailable, orderTotal, validationMessage

2. **Task_CreateFulfilment** - "Create Fulfilment Order"
   - Job Type: `create-fulfilment`
   - Purpose: Creates fulfillment order

3. **Task_HandlePaymentFailure** - "Handle Payment Failure"
   - Job Type: `handle-payment-failure`
   - Purpose: Handles payment errors

4. **Task_HandleOutOfStock** - "Handle Out of Stock"
   - Job Type: `handle-out-of-stock`
   - Purpose: Handles inventory issues

**Worker Location:** `lab2-ecommerce-order/worker/`

---

### Requirement 4: REST Connector for Payment Gateway API
**Status: ✅ IMPLEMENTED**

**REST Connector Task:**
- **Task ID:** Task_ProcessPayment
- **Task Name:** "Process Payment"
- **Connector Type:** `io.camunda:http-json:1` (Official Camunda HTTP JSON Connector)
- **Template:** `io.camunda.connectors.HttpJson.v2`
- **Configuration:**
  - Method: POST
  - URL: Payment gateway endpoint
  - Authentication: Configurable
  - Request Body: paymentToken, orderTotal, currency
  - Response: paymentStatus, transactionId

**When you open in Camunda Modeler:**
- Service task with "R" icon for REST connector
- HTTP Connector template in properties
- Payment API configuration visible

---

### Requirement 5: Process Variables for Data Exchange
**Status: ✅ IMPLEMENTED**

**Variable Flow:**
1. **Start Event** → Order data
2. **Task_ValidateOrder** (Worker)
   - IN: customerRef, productId, quantity, deliveryPreference, customerCategory
   - OUT: orderValid, stockAvailable, orderTotal, productPrice, validationMessage
3. **Gateway_OrderValid** → Routes based on `orderValid`
4. **Task_DeterminePriority** (DMN)
   - IN: orderTotal, customerCategory, deliveryPreference
   - OUT: orderPriority
5. **Gateway_HighValue** → Routes based on order value
6. **Task_ProcessPayment** (REST API)
   - IN: paymentToken, orderTotal
   - OUT: paymentStatus, transactionId
7. **Gateway_PaymentStatus** → Routes based on `paymentStatus`
8. **Task_CreateFulfilment** (Worker)
   - IN: Order details, payment confirmation
   - OUT: fulfilmentId

**Variables Passed Between Activities:**
- ✅ Order details passed to validation worker
- ✅ Validation results (orderValid, stockAvailable, orderTotal) passed to gateways
- ✅ Order total and customer data passed to DMN for priority
- ✅ Payment details passed to REST connector
- ✅ Payment status used for routing decisions

---

## ⚠️ LAB3.BPMN - Hospital Appointment Triage

### Requirement 3: Java/Spring Boot External Job Worker
**Status: ✅ IMPLEMENTED**

**Service Tasks with Job Types:**
1. **Task_ValidateRegistration** - "Validate Registration"
   - Job Type: `validate-registration`
   - Purpose: Validates patient registration
   - Process Variables:
     - Input: patientRef, patientName, appointmentPreference, symptomsDescription, urgencyIndicators
     - Output: registrationValid, validationErrors

2. **Task_BookAppointment** - "Book Appointment"
   - Job Type: `book-appointment`
   - Purpose: Books appointment slots

**Worker Location:** `lab3-hospital-appointment/worker/`

---

### Requirement 4: REST Connector for Scheduling API
**Status: ⚠️ NEEDS UPDATE**

**Current Implementation:**
- Uses generic HTTP task with task headers
- NOT using official `io.camunda:http-json:1` connector

**What you'll see in Camunda Modeler:**
- Service task WITHOUT the "R" REST connector icon
- Task definition type: "http" (generic)
- Configuration in task headers instead of I/O mappings

**RECOMMENDED FIX:**
Should be updated to use `io.camunda:http-json:1` connector like Lab 1 and Lab 2 for:
- Better error handling
- Consistent REST integration
- Proper incident management
- Visual REST connector icon in modeler

**Tasks that need REST connector:**
1. **Task_CheckAvailabilityPriority** - Check Appointment Availability (Priority)
2. **Task_CheckAvailabilityRoutine** - Check Appointment Availability (Routine)

---

### Requirement 5: Process Variables for Data Exchange
**Status: ✅ IMPLEMENTED**

**Variable Flow:**
1. **Start Event** → Patient data
2. **Task_ValidateRegistration** (Worker)
   - IN: patientRef, patientName, appointmentPreference, symptomsDescription, urgencyIndicators
   - OUT: registrationValid, validationErrors
3. **Gateway_RegistrationValid** → Routes based on `registrationValid`
4. **Task_ClassifyTriage** (DMN)
   - IN: symptomsDescription, urgencyIndicators
   - OUT: triageCategory
5. **Gateway_TriageCategory** → Routes based on `triageCategory`
   - "UrgentAssessment" → User Task (Emergency Staff)
   - "PriorityReview" → Check Priority Slots
   - "Routine" → Check Routine Slots
6. **Task_CheckAvailability** (HTTP API)
   - IN: appointmentPreference, priority level
   - OUT: availableSlots
7. **Gateway_SlotAvailable** → Routes based on slot availability
8. **Task_BookAppointment** (Worker)
   - IN: Selected slot, patient details
   - OUT: bookingStatus, appointmentId

**Variables Passed Between Activities:**
- ✅ Patient details passed to validation worker
- ✅ Validation results passed to DMN triage
- ✅ Triage category used for routing
- ✅ Appointment preferences passed to scheduling API
- ✅ Slot availability used for booking decisions

---

## Summary Checklist

### Lab 1: Loan Approval ✅
- ✅ Worker: `validate-applicant` job type
- ✅ REST Connector: `io.camunda:http-json:1` for credit API
- ✅ Process Variables: Complete flow from input → validation → credit API → DMN → decision

### Lab 2: E-Commerce Order ✅
- ✅ Worker: `validate-order` job type
- ✅ REST Connector: `io.camunda:http-json:1` for payment API
- ✅ Process Variables: Complete flow from input → validation → DMN → payment API → fulfillment

### Lab 3: Hospital Appointment ⚠️
- ✅ Worker: `validate-registration` job type
- ⚠️ REST Connector: Uses HTTP task (should use `io.camunda:http-json:1`)
- ✅ Process Variables: Complete flow from input → validation → DMN → scheduling → booking

---

## How to Verify in Camunda Modeler

### 1. Open Each BPMN File
```
File → Open → Select lab1.bpmn / lab2.bpmn / lab3.bpmn
```

### 2. Check for Service Tasks (Workers)
- Look for tasks with **gear icon** ⚙️
- Click on task → Properties Panel → Type should show "Service Task"
- Task Definition → Type field shows the job type (e.g., `validate-applicant`)

### 3. Check for REST Connectors
- **Lab 1 & 2:** Look for tasks with **"R" icon** or lightning bolt
- Task should show "HTTP Connector" template
- Properties panel shows HTTP configuration (URL, method, headers)
- **Lab 3:** Currently shows generic HTTP task (needs update to REST connector)

### 4. Check for DMN Business Rule Tasks
- Look for tasks with **table icon** 📋
- Task type: "Business Rule Task"
- Called Decision shows DMN table ID (e.g., `loan-decision`)

### 5. Check Process Variables
- Click on each task
- Check "Input/Output" tab in properties
- See input mappings (variables going IN to task)
- See output mappings (variables coming OUT of task)

### 6. Check Gateways
- Click on diamond-shaped gateways
- Check sequence flow conditions
- Verify they reference correct variables (e.g., `loanDecision`, `orderValid`, `triageCategory`)

---

## Final Answer

### Are Requirements 3, 4, 5 Fulfilled?

#### Lab 1: ✅ YES - FULLY COMPLIANT
- ✅ Worker with job type
- ✅ REST connector (proper implementation)
- ✅ Complete variable passing

#### Lab 2: ✅ YES - FULLY COMPLIANT
- ✅ Worker with job type
- ✅ REST connector (proper implementation)
- ✅ Complete variable passing

#### Lab 3: ⚠️ MOSTLY YES - MINOR ISSUE
- ✅ Worker with job type
- ⚠️ HTTP task (functional but should use REST connector for consistency)
- ✅ Complete variable passing

**Recommendation:** Update Lab 3 to use `io.camunda:http-json:1` connector type for full compliance and consistency across all labs.

---

**Created:** January 2024  
**For:** Camunda Modeler Verification
