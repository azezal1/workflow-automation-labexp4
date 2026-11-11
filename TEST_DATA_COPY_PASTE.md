# Copy-Paste Test Data for All Labs

---

## LAB 1: Loan Approval Process

### Test Case 1: Auto-Approve
```json
{
  "applicantId": "APP001",
  "applicantName": "Jane Smith",
  "requestedAmount": 50000,
  "monthlyIncome": 8000,
  "employmentStatus": "EMPLOYED"
}
```

### Test Case 2: Auto-Reject
```json
{
  "applicantId": "APP002",
  "applicantName": "Bob Johnson",
  "requestedAmount": 100000,
  "monthlyIncome": 2000,
  "employmentStatus": "UNEMPLOYED"
}
```

### Test Case 3: Manual Review
```json
{
  "applicantId": "APP003",
  "applicantName": "Alice Williams",
  "requestedAmount": 45000,
  "monthlyIncome": 4500,
  "employmentStatus": "EMPLOYED"
}
```

### Manual Review User Task - Output Variables
```json
{
  "manualDecision": "Approved",
  "reviewNotes": "Good credit history and stable employment"
}
```

---

## LAB 2: E-Commerce Order Processing

### Test Case 1: Normal Order (Auto-Fulfill)
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

### Test Case 2: High-Value Order (Manual Verification)
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

### Test Case 3: Out of Stock
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

### Test Case 4: Payment Declined
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

### Manual Verification User Task - Output Variables
```json
{
  "verificationDecision": "Approved",
  "verificationNotes": "Legitimate premium customer, order approved"
}
```

---

## LAB 3: Hospital Appointment Triage

### Test Case 1: Routine Appointment
```json
{
  "patientRef": "PAT001",
  "patientName": "John Doe",
  "appointmentPreference": "morning",
  "symptomsDescription": "annual physical checkup",
  "urgencyIndicators": ""
}
```

### Test Case 2: Priority Review
```json
{
  "patientRef": "PAT002",
  "patientName": "Mary Johnson",
  "appointmentPreference": "afternoon",
  "symptomsDescription": "persistent moderate headache for 3 days",
  "urgencyIndicators": "persistent, moderate"
}
```

### Test Case 3: Urgent Assessment
```json
{
  "patientRef": "PAT003",
  "patientName": "Robert Williams",
  "appointmentPreference": "immediate",
  "symptomsDescription": "severe chest pain with difficulty breathing",
  "urgencyIndicators": "chest pain, difficulty breathing, severe"
}
```

### Test Case 4: No Slots Available
```json
{
  "patientRef": "PAT004",
  "patientName": "Susan Brown",
  "appointmentPreference": "evening",
  "symptomsDescription": "routine followup consultation",
  "urgencyIndicators": ""
}
```

### Staff Review User Task - Output Variables
```json
{
  "staffDecision": "Approved",
  "reviewNotes": "Priority case, accommodated in emergency slot"
}
```

### Urgent Assessment User Task - Output Variables
```json
{
  "urgentAssessmentNotes": "Patient requires immediate emergency care",
  "urgentAction": "EMERGENCY_PROTOCOL"
}
```
