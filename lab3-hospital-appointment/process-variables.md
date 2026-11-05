# Lab 3: Process Variables Dictionary

## Hospital Appointment and Patient Triage Management - Process Variables

This document defines all process variables used in the Hospital Appointment workflow, including their data types, sources, and purposes.

| Variable Name | Data Type | Source | Direction | Description | Example Value |
|--------------|-----------|--------|-----------|-------------|---------------|
| `patientRef` | String | Process Start | Input | Unique synthetic patient reference identifier | `"PAT-2024-001"` |
| `patientName` | String | Process Start | Input | Patient full name (synthetic data) | `"John Doe"` |
| `appointmentPreference` | String | Process Start | Input | Type of appointment requested by patient | `"General Consultation"` or `"Specialist Consultation"` |
| `symptomsDescription` | String | Process Start | Input | Brief summary of patient symptoms for triage | `"Persistent headache for 3 days, mild fever"` |
| `urgencyIndicators` | Object/Map | Process Start | Input | Predefined urgency indicators for DMN evaluation | `{ "pain_level": 6, "breathing_difficulty": false, "bleeding": false, "chronic_condition": true }` |
| `registrationValid` | Boolean | Validation Worker | Output | Result of patient registration validation | `true` or `false` |
| `validationErrors` | Array[String] | Validation Worker | Output | List of validation errors if registration fails | `["Missing patient date of birth", "Invalid contact number"]` |
| `triageCategory` | String | DMN Decision | Output | Triage classification from DMN decision table | `"Routine"`, `"Priority Review"`, or `"Urgent Assessment"` |
| `availableSlots` | Array[Object] | REST Connector | Output | List of available appointment slots from scheduling API | `[{ "slotId": "SLOT-001", "dateTime": "2024-06-15T09:00:00", "physician": "Dr. Smith" }]` |
| `slotId` | String | User Task / Gateway | Internal | Selected appointment slot identifier | `"SLOT-2024-06-15-001"` |
| `bookingStatus` | String | REST Connector / Gateway | Output | Status of appointment booking attempt | `"Confirmed"`, `"Pending"`, `"Failed"`, or `"No Slots Available"` |
| `bookingConfirmation` | Object | REST Connector | Output | Appointment booking confirmation details | `{ "confirmationNumber": "CONF-2024-001", "appointmentDateTime": "2024-06-15T09:00:00", "department": "General Medicine", "instructions": "Arrive 15 minutes early" }` |

## Variable Flow Through Process

### 1. Process Initiation
- **Input Variables**: `patientRef`, `patientName`, `appointmentPreference`, `symptomsDescription`, `urgencyIndicators`
- **Purpose**: Capture patient appointment request and initial triage indicators

### 2. Registration Validation (External Worker)
- **Input Variables**: `patientRef`, `patientName`, `appointmentPreference`, `symptomsDescription`
- **Output Variables**: `registrationValid`, `validationErrors`
- **Purpose**: Validate registration fields and data integrity

### 3. Triage Classification (DMN Decision)
- **Input Variables**: `urgencyIndicators`, `symptomsDescription`, `appointmentPreference`
- **Output Variables**: `triageCategory`
- **Purpose**: Classify patient request based on predefined educational rules

### 4. Appointment Availability Check (REST Connector)
- **Input Variables**: `patientRef`, `appointmentPreference`, `triageCategory`
- **Output Variables**: `availableSlots`, `bookingStatus`
- **Purpose**: Query scheduling API for available appointment slots

### 5. Routing Gateways
- **Decision Variables**: `registrationValid`, `triageCategory`, `bookingStatus`, `availableSlots`
- **Purpose**: Route workflow based on validation, triage category, and slot availability

### 6. Manual Review (User Task)
- **Input Variables**: All patient and triage variables
- **Output Variables**: `slotId`, `bookingStatus`
- **Purpose**: Staff review for priority cases or when no automated booking is possible

### 7. Booking Confirmation
- **Input Variables**: `patientRef`, `slotId`
- **Output Variables**: `bookingConfirmation`
- **Purpose**: Confirm appointment booking and provide patient instructions

## Variable Initialization Examples

### Example 1: Routine Appointment Request
```json
{
  "patientRef": "PAT-2024-001",
  "patientName": "Jane Smith",
  "appointmentPreference": "General Consultation",
  "symptomsDescription": "Annual checkup, no acute symptoms",
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

### Example 2: Priority Review Request
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

### Example 3: Urgent Assessment Request
```json
{
  "patientRef": "PAT-2024-003",
  "patientName": "Maria Garcia",
  "appointmentPreference": "Emergency Assessment",
  "symptomsDescription": "Severe abdominal pain, vomiting, fever",
  "urgencyIndicators": {
    "pain_level": 9,
    "breathing_difficulty": false,
    "bleeding": false,
    "chronic_condition": false,
    "temperature_celsius": 39.5,
    "duration_days": 1,
    "vomiting": true
  }
}
```

## Notes on Process Variables

1. **Synthetic Data**: All patient references, names, and medical information are synthetic and created for educational purposes only.

2. **Variable Scope**: All process variables are scoped to the individual process instance and are not shared across instances.

3. **Data Privacy**: In a production implementation, sensitive patient data would require encryption, access controls, and compliance with healthcare regulations (HIPAA, GDPR).

4. **Error Handling**: When service tasks fail, variables such as `validationErrors` capture error details for incident diagnosis and recovery.

5. **DMN Input Mapping**: The `urgencyIndicators` object must be properly mapped to individual DMN input columns for correct triage evaluation.

6. **REST Connector Mapping**: The REST Connector uses input variable mapping to construct API requests and output variable mapping to extract response data into process variables.
