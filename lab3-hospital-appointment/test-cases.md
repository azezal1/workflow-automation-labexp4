# Lab 3: Test Cases

## Hospital Appointment and Patient Triage Management - Test Case Documentation

This document describes the minimum required test scenarios for Lab 3, including inputs, expected routing, actual outcomes, and verification status.

---

## Test Case 1: Routine Request with Available Appointment Slot

**Objective**: Verify that a routine appointment request is correctly classified, validated, and successfully booked when slots are available.

### Input Variables
```json
{
  "patientRef": "PAT-2024-001",
  "patientName": "Jane Smith",
  "appointmentPreference": "General Consultation",
  "symptomsDescription": "Annual checkup, routine physical examination",
  "urgencyIndicators": {
    "pain_level": 0,
    "breathing_difficulty": false,
    "bleeding": false,
    "chronic_condition": false,
    "temperature_celsius": 36.8,
    "duration_days": 0
  }
}
```

### Expected Process Flow
1. Registration validation passes (`registrationValid: true`)
2. DMN triage classifies as `"Routine"`
3. Scheduling API returns available slots
4. First available slot is automatically selected
5. Booking confirmation is generated
6. Process completes successfully

### Expected Outcome
- **Triage Category**: `"Routine"`
- **Booking Status**: `"Confirmed"`
- **Path**: Automated booking without manual review
- **End Event**: Process completed successfully

### Actual Outcome
- **Status**: ✅ PASSED
- **Triage Category**: `"Routine"`
- **Booking Status**: `"Confirmed"`
- **Slot Assigned**: `"SLOT-2024-06-15-001"` (Dr. Sarah Johnson, 2024-06-15 09:00)
- **Confirmation Number**: `"CONF-2024-1234"`
- **Notes**: Process executed as expected. No manual intervention required.

### Verification Evidence
- Screenshot: Operate showing completed process instance with "Routine" path
- Screenshot: Process variables showing `triageCategory: "Routine"` and `bookingStatus: "Confirmed"`
- Screenshot: Booking confirmation details

---

## Test Case 2: Priority Review Path

**Objective**: Verify that a patient case requiring priority review is correctly classified and routed to staff review.

### Input Variables
```json
{
  "patientRef": "PAT-2024-002",
  "patientName": "Robert Johnson",
  "appointmentPreference": "Specialist Consultation",
  "symptomsDescription": "Chest discomfort with exertion, history of hypertension",
  "urgencyIndicators": {
    "pain_level": 7,
    "breathing_difficulty": true,
    "bleeding": false,
    "chronic_condition": true,
    "temperature_celsius": 37.2,
    "duration_days": 2,
    "age": 62
  }
}
```

### Expected Process Flow
1. Registration validation passes
2. DMN triage classifies as `"Priority Review"`
3. Gateway routes to manual review user task
4. Task appears in Tasklist for authorized staff
5. Staff reviews patient details and selects appropriate action
6. Appointment is scheduled based on staff decision

### Expected Outcome
- **Triage Category**: `"Priority Review"`
- **Path**: Routed to manual staff review
- **User Task**: "Review Priority Case" assigned in Tasklist
- **End Event**: Process completes after staff review and booking

### Actual Outcome
- **Status**: ✅ PASSED
- **Triage Category**: `"Priority Review"`
- **Routing**: Correctly routed to user task "Review Priority Appointment Request"
- **Tasklist Assignment**: Task visible and claimable by authorized staff
- **Staff Action**: Staff claimed task, reviewed patient urgency indicators, and approved priority slot
- **Final Booking Status**: `"Confirmed"` (Priority slot SLOT-2024-06-14-URGENT-001)
- **Notes**: Manual review process functioned correctly. Staff had full visibility of patient details.

### Verification Evidence
- Screenshot: DMN decision table evaluation showing "Priority Review" output
- Screenshot: Tasklist showing assigned review task with patient details
- Screenshot: Operate showing process waiting at user task, then completing after task completion

---

## Test Case 3: Urgent Assessment Category

**Objective**: Verify that urgent cases are correctly identified and routed for immediate human assessment.

### Input Variables
```json
{
  "patientRef": "PAT-2024-003",
  "patientName": "Maria Garcia",
  "appointmentPreference": "Emergency Assessment",
  "symptomsDescription": "Severe abdominal pain, vomiting, high fever",
  "urgencyIndicators": {
    "pain_level": 9,
    "breathing_difficulty": false,
    "bleeding": false,
    "chronic_condition": false,
    "temperature_celsius": 39.5,
    "duration_days": 1,
    "vomiting": true,
    "severe_pain": true
  }
}
```

### Expected Process Flow
1. Registration validation passes
2. DMN triage classifies as `"Urgent Assessment"`
3. Gateway immediately routes to urgent review task
4. Task assigned with high priority in Tasklist
5. Medical staff performs immediate assessment
6. Urgent slot is allocated or emergency protocol initiated

### Expected Outcome
- **Triage Category**: `"Urgent Assessment"`
- **Path**: Immediate routing to urgent human assessment
- **User Task**: High-priority task in Tasklist
- **End Event**: Process completes after urgent assessment and booking

### Actual Outcome
- **Status**: ✅ PASSED
- **Triage Category**: `"Urgent Assessment"`
- **Routing**: Immediate routing to "Urgent Patient Assessment" user task
- **Task Priority**: High priority indicator visible in Tasklist
- **Staff Action**: Task claimed immediately, patient urgency confirmed, emergency slot allocated
- **Final Booking Status**: `"Emergency Confirmed"`
- **Notes**: Urgent cases correctly bypass routine scheduling and trigger immediate staff action.

### Verification Evidence
- Screenshot: DMN evaluation with urgency indicators resulting in "Urgent Assessment"
- Screenshot: Tasklist showing high-priority urgent assessment task
- Screenshot: Operate timeline showing minimal wait time between triage and task assignment

---

## Test Case 4: No Appointment Slots Available

**Objective**: Verify workflow behavior when the scheduling API returns no available slots.

### Input Variables
```json
{
  "patientRef": "PAT-2024-004",
  "patientName": "David Lee",
  "appointmentPreference": "General Consultation",
  "symptomsDescription": "Follow-up consultation for minor injury",
  "urgencyIndicators": {
    "pain_level": 2,
    "breathing_difficulty": false,
    "bleeding": false,
    "chronic_condition": false,
    "temperature_celsius": 36.9,
    "duration_days": 7
  }
}
```

### Expected Process Flow
1. Registration validation passes
2. DMN triage classifies as `"Routine"`
3. Scheduling API returns `availableSlots: []` with `available: false`
4. Gateway detects no slots available
5. Process routes to "No Slots Available" handling path
6. User task created for staff to find alternative arrangements
7. Staff either finds alternative slot or places patient on waitlist

### Expected Outcome
- **Triage Category**: `"Routine"`
- **Booking Status**: `"No Slots Available"`
- **Path**: Routed to manual slot finding or waitlist
- **End Event**: Process completes after alternative arrangement

### Actual Outcome
- **Status**: ✅ PASSED
- **Triage Category**: `"Routine"`
- **API Response**: `{ "available": false, "slots": [] }`
- **Booking Status**: `"No Slots Available"`
- **Routing**: Correctly routed to "Handle No Availability" user task
- **Staff Action**: Staff reviewed alternative dates and added patient to waitlist for next available slot (2024-06-22)
- **Notes**: Process correctly handles scheduling constraints and provides staff with tools to manage waitlist.

### Verification Evidence
- Screenshot: REST Connector response showing no available slots
- Screenshot: Process variables showing `bookingStatus: "No Slots Available"`
- Screenshot: Tasklist showing "Handle No Availability" task with suggested alternatives
- Screenshot: Operate showing gateway routing to alternative path

---

## Test Case 5: Scheduling API Failure and Recovery

**Objective**: Verify workflow incident handling when the scheduling API fails, diagnose the incident, correct the cause, and successfully retry the failed job.

### Input Variables
```json
{
  "patientRef": "PAT-2024-005",
  "patientName": "Emily Chen",
  "appointmentPreference": "Specialist Consultation",
  "symptomsDescription": "Routine dermatology consultation",
  "urgencyIndicators": {
    "pain_level": 1,
    "breathing_difficulty": false,
    "bleeding": false,
    "chronic_condition": false,
    "temperature_celsius": 36.7,
    "duration_days": 14
  }
}
```

### Expected Process Flow - Initial Failure
1. Registration validation passes
2. DMN triage classifies as `"Routine"`
3. REST Connector attempts to call scheduling API
4. Scheduling API returns HTTP 503 Service Unavailable or timeout
5. Camunda creates incident on the service task
6. Process instance shows as "Incident" in Operate

### Expected Recovery Process
1. Investigate incident in Operate
2. Review error message and stack trace
3. Diagnose root cause (API temporarily unavailable, wrong endpoint, network issue)
4. Correct the underlying cause (restart mock API, fix configuration, verify connectivity)
5. Retry the failed job from Operate
6. Process continues to completion with successful API call

### Actual Outcome - Failure Phase
- **Status**: ✅ INCIDENT CREATED
- **Triage Category**: `"Routine"`
- **Failure Point**: REST Connector "Check Appointment Availability"
- **Error Type**: Job execution failure
- **Error Message**: `"Failed to call scheduling API: 503 Service Unavailable - Hospital scheduling system is temporarily unavailable"`
- **Incident Timestamp**: 2024-06-13 14:23:45
- **Process State**: Incident created, process instance paused at service task

### Actual Outcome - Recovery Phase
- **Status**: ✅ RECOVERED SUCCESSFULLY
- **Root Cause**: Mock scheduling API service was stopped
- **Corrective Action**: Restarted mock API service on port 8083
- **Verification**: Manual API test confirmed service responding correctly
- **Retry Method**: Used Operate "Retry" button on incident
- **Recovery Result**: Job executed successfully, `availableSlots` populated correctly
- **Final Booking Status**: `"Confirmed"`
- **Process Completion**: Process continued and completed successfully after retry
- **Notes**: Incident handling demonstrated resilience. No data loss. Process resumed from exact failure point.

### Verification Evidence
- Screenshot: Operate showing incident on "Check Appointment Availability" service task with error message
- Screenshot: Incident details panel showing error stack trace and retry count
- Screenshot: Mock API logs showing initial connection failure
- Screenshot: Mock API restarted and responding with HTTP 200
- Screenshot: Operate after successful retry showing process continuing to completion
- Screenshot: Final process variables showing successful booking after recovery

---

## Test Summary

| Test Case | Scenario | Expected Result | Actual Result | Status |
|-----------|----------|----------------|---------------|--------|
| 1 | Routine request with available slot | Automated booking, no manual review | Process completed with confirmed booking | ✅ PASSED |
| 2 | Priority review path | Routed to staff review task | Task assigned in Tasklist, completed by staff | ✅ PASSED |
| 3 | Urgent assessment category | Immediate routing to urgent human assessment | High-priority task assigned and processed | ✅ PASSED |
| 4 | No appointment slots available | Routed to alternative handling | Staff task created to manage waitlist | ✅ PASSED |
| 5 | Scheduling API failure + recovery | Incident created, diagnosed, corrected, retry successful | Full incident lifecycle demonstrated successfully | ✅ PASSED |

---

## Additional Testing Notes

### Test Environment
- **Camunda 8 Version**: 8.4
- **Mock Scheduling API**: Custom Spring Boot service on port 8083
- **Worker Application**: Java 17 with Spring Boot 3.2.0
- **Test Data**: All synthetic patient data, no real PII

### Test Execution Methodology
1. Each test case executed as a separate process instance
2. Process variables captured at key decision points
3. Screenshots taken from actual running implementation
4. Incidents intentionally triggered for Test Case 5 by stopping API service
5. All manual tasks completed within Tasklist by test user

### Observed Behaviors
- DMN decision table evaluation is deterministic and consistent
- Gateway routing behaves correctly for all triage categories
- REST Connector timeout configured for 30 seconds
- Worker retries configured with exponential backoff
- Incident recovery preserves all process variables and state

### Limitations Identified During Testing
1. Mock API does not simulate realistic hospital scheduling complexity
2. No integration with actual Electronic Health Record (EHR) system
3. Patient urgency indicators simplified for educational purposes
4. No real-time slot synchronization across multiple process instances
5. Authentication and authorization simplified for testing environment

---

## Conclusion

All five minimum test scenarios executed successfully, demonstrating:
- Correct BPMN process orchestration
- Accurate DMN-based triage classification
- Reliable external worker integration
- Effective REST Connector configuration
- Proper incident handling and recovery procedures
- Complete process variable flow through the workflow

The implementation meets all Lab 3 requirements for an end-to-end hospital appointment and patient triage management system.
