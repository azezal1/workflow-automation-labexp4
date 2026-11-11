# Process Variables - All 3 Labs

Complete list of variables for lab1.bpmn, lab2.bpmn, and lab3.bpmn

---

## 📋 LAB 1: Loan Approval Process

### Start Event Variables (Input)

```json
{
  "applicantId": "APP001",
  "applicantName": "John Doe",
  "requestedAmount": 50000,
  "monthlyIncome": 6000,
  "employmentStatus": "EMPLOYED"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| applicantId | String | Unique applicant identifier | "APP001" |
| applicantName | String | Full name of applicant | "John Doe" |
| requestedAmount | Number | Loan amount requested | 50000 |
| monthlyIncome | Number | Monthly income in dollars | 6000 |
| employmentStatus | String | Employment status | "EMPLOYED", "UNEMPLOYED", "SELF_EMPLOYED" |

---

### Worker Output Variables (validate-applicant)

```json
{
  "validationPassed": true,
  "validationErrors": ""
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| validationPassed | Boolean | Validation result | true or false |
| validationErrors | String | Error messages if validation fails | "Invalid employment status" |

---

### REST API Output Variables (Credit Assessment)

```json
{
  "creditScore": 720,
  "creditStatus": "GOOD"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| creditScore | Number | Credit score (300-850) | 720 |
| creditStatus | String | Credit status category | "EXCELLENT", "GOOD", "FAIR", "POOR" |

---

### DMN Output Variables (loan-decision)

```json
{
  "loanDecision": "Approved"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| loanDecision | String | DMN decision result | "Approved", "Rejected", "Manual Review" |

---

### User Task Output Variables (Manual Review)

```json
{
  "manualDecision": "Approved",
  "reviewNotes": "Applicant has stable employment and good payment history"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| manualDecision | String | Manual review decision | "Approved", "Rejected" |
| reviewNotes | String | Reviewer's comments | "Good credit history" |

---

### Complete Lab 1 Test Data Examples

#### Test Case 1: Auto-Approve
```json
{
  "applicantId": "APP001",
  "applicantName": "Jane Smith",
  "requestedAmount": 50000,
  "monthlyIncome": 8000,
  "employmentStatus": "EMPLOYED"
}
```
**Expected:** Auto-approved (high income, good credit)

---

#### Test Case 2: Auto-Reject
```json
{
  "applicantId": "APP002",
  "applicantName": "Bob Johnson",
  "requestedAmount": 100000,
  "monthlyIncome": 2000,
  "employmentStatus": "UNEMPLOYED"
}
```
**Expected:** Auto-rejected (low income, unemployed)

---

#### Test Case 3: Manual Review
```json
{
  "applicantId": "APP003",
  "applicantName": "Alice Williams",
  "requestedAmount": 45000,
  "monthlyIncome": 4500,
  "employmentStatus": "EMPLOYED"
}
```
**Expected:** Routes to manual review

---

## 📋 LAB 2: E-Commerce Order Processing

### Start Event Variables (Input)

```json
{
  "customerRef": "CUST123",
  "productId": "P001",
  "quantity": 5,
  "deliveryPreference": "EXPRESS",
  "paymentToken": "tok_success",
  "customerCategory": "PREMIUM"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| customerRef | String | Customer reference ID | "CUST123" |
| productId | String | Product identifier | "P001", "P002", "P003" |
| quantity | Number | Order quantity | 5 |
| deliveryPreference | String | Delivery type | "EXPRESS", "STANDARD", "ECONOMY" |
| paymentToken | String | Payment token/method | "tok_success", "tok_fail" |
| customerCategory | String | Customer tier | "PREMIUM", "GOLD", "SILVER", "BRONZE" |

---

### Worker Output Variables (validate-order)

```json
{
  "orderValid": true,
  "stockAvailable": true,
  "orderTotal": 149.95,
  "productPrice": 29.99,
  "validationMessage": "Order validated successfully"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| orderValid | Boolean | Order validation result | true or false |
| stockAvailable | Boolean | Inventory check result | true or false |
| orderTotal | Number | Total order amount | 149.95 |
| productPrice | Number | Unit price | 29.99 |
| validationMessage | String | Validation status message | "Order validated" |

---

### DMN Output Variables (order-priority-decision)

```json
{
  "orderPriority": "Critical"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| orderPriority | String | Priority classification | "Critical", "High", "Medium", "Standard" |

---

### REST API Output Variables (Payment Gateway)

```json
{
  "paymentStatus": "success",
  "transactionId": "TXN123456"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| paymentStatus | String | Payment result | "success", "failed", "declined" |
| transactionId | String | Transaction reference | "TXN123456" |

---

### User Task Output Variables (Manual Verification)

```json
{
  "verificationDecision": "Approved",
  "verificationNotes": "Legitimate premium customer, order approved"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| verificationDecision | String | Verification decision | "Approved", "Rejected" |
| verificationNotes | String | Verification comments | "Fraud check passed" |

---

### Complete Lab 2 Test Data Examples

#### Test Case 1: Normal Order (Auto-Fulfill)
```json
{
  "customerRef": "CUST123",
  "productId": "P001",
  "quantity": 5,
  "deliveryPreference": "STANDARD",
  "paymentToken": "tok_success",
  "customerCategory": "GOLD"
}
```
**Expected:** Auto-fulfilled (stock available, payment success, order < $1000)

---

#### Test Case 2: High-Value Order (Manual Verification)
```json
{
  "customerRef": "CUST456",
  "productId": "P002",
  "quantity": 10,
  "deliveryPreference": "EXPRESS",
  "paymentToken": "tok_success",
  "customerCategory": "PREMIUM"
}
```
**Expected:** Routes to manual verification (orderTotal = $1499.90 >= $1000)

---

#### Test Case 3: Out of Stock
```json
{
  "customerRef": "CUST789",
  "productId": "P001",
  "quantity": 150,
  "deliveryPreference": "STANDARD",
  "paymentToken": "tok_success",
  "customerCategory": "SILVER"
}
```
**Expected:** Out of stock notification (quantity > 100)

---

#### Test Case 4: Payment Declined
```json
{
  "customerRef": "CUST999",
  "productId": "P003",
  "quantity": 3,
  "deliveryPreference": "ECONOMY",
  "paymentToken": "tok_declined",
  "customerCategory": "BRONZE"
}
```
**Expected:** Payment failure handling

---

### Product Catalog (for reference)

| Product ID | Name | Price | Max Stock |
|------------|------|-------|-----------|
| P001 | Widget | $29.99 | 100 units |
| P002 | Gadget | $149.99 | 100 units |
| P003 | Accessory | $9.99 | 100 units |

---

## 📋 LAB 3: Hospital Appointment Triage

### Start Event Variables (Input)

```json
{
  "patientRef": "PAT001",
  "patientName": "Jane Smith",
  "appointmentPreference": "morning",
  "symptomsDescription": "routine checkup",
  "urgencyIndicators": ""
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| patientRef | String | Patient reference ID | "PAT001" |
| patientName | String | Patient full name | "Jane Smith" |
| appointmentPreference | String | Preferred time | "morning", "afternoon", "evening" |
| symptomsDescription | String | Description of symptoms | "routine checkup", "severe headache" |
| urgencyIndicators | String | Urgency keywords | "chest pain", "difficulty breathing", "" |

---

### Worker Output Variables (validate-patient-registration)

```json
{
  "registrationValid": true,
  "validationErrors": ""
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| registrationValid | Boolean | Registration validation result | true or false |
| validationErrors | String | Error messages if invalid | "Invalid patient reference" |

---

### DMN Output Variables (triage-decision)

```json
{
  "triageCategory": "Routine"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| triageCategory | String | Triage classification | "Routine", "PriorityReview", "UrgentAssessment" |

---

### REST API Output Variables (Scheduling API)

```json
{
  "slotsAvailable": true,
  "availableSlots": ["2024-01-15 09:00", "2024-01-15 14:00"]
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| slotsAvailable | Boolean | Slot availability status | true or false |
| availableSlots | Array | List of available time slots | ["2024-01-15 09:00"] |

---

### Worker Output Variables (book-appointment)

```json
{
  "bookingStatus": "CONFIRMED",
  "appointmentId": "APPT123456",
  "appointmentDateTime": "2024-01-15 09:00"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| bookingStatus | String | Booking result | "CONFIRMED", "FAILED" |
| appointmentId | String | Appointment reference | "APPT123456" |
| appointmentDateTime | String | Booked time slot | "2024-01-15 09:00" |

---

### User Task Output Variables (Staff Review)

```json
{
  "staffDecision": "Approved",
  "reviewNotes": "Priority case, accommodated in emergency slot"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| staffDecision | String | Staff review decision | "Approved", "Rejected" |
| reviewNotes | String | Staff comments | "Emergency accommodation" |

---

### User Task Output Variables (Urgent Assessment)

```json
{
  "urgentAssessmentNotes": "Patient requires immediate emergency care",
  "urgentAction": "EMERGENCY_PROTOCOL"
}
```

| Variable | Type | Description | Example |
|----------|------|-------------|---------|
| urgentAssessmentNotes | String | Emergency assessment notes | "Immediate care required" |
| urgentAction | String | Action taken | "EMERGENCY_PROTOCOL", "ICU_ADMISSION" |

---

### Complete Lab 3 Test Data Examples

#### Test Case 1: Routine Appointment (Auto-Book)
```json
{
  "patientRef": "PAT001",
  "patientName": "John Doe",
  "appointmentPreference": "morning",
  "symptomsDescription": "annual physical checkup",
  "urgencyIndicators": ""
}
```
**Expected:** Auto-booked routine appointment

---

#### Test Case 2: Priority Review
```json
{
  "patientRef": "PAT002",
  "patientName": "Mary Johnson",
  "appointmentPreference": "afternoon",
  "symptomsDescription": "persistent moderate headache for 3 days",
  "urgencyIndicators": "persistent, moderate"
}
```
**Expected:** Routes to staff review for priority handling

---

#### Test Case 3: Urgent Assessment
```json
{
  "patientRef": "PAT003",
  "patientName": "Robert Williams",
  "appointmentPreference": "immediate",
  "symptomsDescription": "severe chest pain with difficulty breathing",
  "urgencyIndicators": "chest pain, difficulty breathing, severe"
}
```
**Expected:** Routes to urgent assessment by emergency staff

---

#### Test Case 4: No Slots Available
```json
{
  "patientRef": "PAT004",
  "patientName": "Susan Brown",
  "appointmentPreference": "evening",
  "symptomsDescription": "routine followup consultation",
  "urgencyIndicators": ""
}
```
**Expected:** Routes to staff review (if no slots available)

---

### Triage Keywords for DMN

**Urgent Assessment triggers:**
- "chest pain"
- "difficulty breathing"
- "unconscious"
- "severe bleeding"
- "stroke symptoms"

**Priority Review triggers:**
- "persistent"
- "moderate"
- "recurring"
- "concerning"

**Routine:**
- "checkup"
- "routine"
- "followup"
- "minor"

---

## 🔧 Using Variables in Camunda Modeler

### To Start Instance with Variables:

1. **Open BPMN file in Camunda Modeler**
2. **Click "Start Instance" button**
3. **Paste JSON variables** (from examples above)
4. **Click "Start"**

### To View Variables in Operate:

1. **Open Operate:** http://localhost:8081
2. **Click on process instance**
3. **View "Variables" tab**
4. **See all current variable values**

### To Complete User Tasks in Tasklist:

1. **Open Tasklist:** http://localhost:8082
2. **Click on task**
3. **View input variables** (read-only)
4. **Fill output variables** (your decision)
5. **Click "Complete Task"**

---

## 📊 Variable Summary Table

| Lab | Input Variables | Worker Variables | REST API Variables | DMN Variables | User Task Variables |
|-----|----------------|------------------|-------------------|---------------|-------------------|
| Lab 1 | 5 | 2 | 2 | 1 | 2 |
| Lab 2 | 6 | 5 | 2 | 1 | 2 |
| Lab 3 | 5 | 2 | 2 | 1 | 4 |

---

**Created:** January 2024  
**Repository:** https://github.com/azezal1/workflow-automation-labexp4
