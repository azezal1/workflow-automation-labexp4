# Lab 1: Loan Approval Process Variables

This document describes all process variables used in the Loan Approval workflow, their types, sources, and how they flow between activities.

## Process Variables Dictionary

| Variable Name | Type | Source Task | Target Tasks | Description |
|--------------|------|-------------|--------------|-------------|
| `applicantId` | String | Start Event (User Input) | Validate Applicant Info, Assess Credit, Decision Table, Manual Review | Unique identifier for the loan applicant (e.g., APP-2024-001) |
| `applicantName` | String | Start Event (User Input) | Validate Applicant Info, Assess Credit, Manual Review | Full name of the loan applicant |
| `requestedAmount` | Double | Start Event (User Input) | Validate Applicant Info, Decision Table, Manual Review | Loan amount requested by the applicant in currency units |
| `monthlyIncome` | Double | Start Event (User Input) | Validate Applicant Info, Decision Table, Assess Credit | Applicant's verified monthly income in currency units |
| `employmentStatus` | String | Start Event (User Input) | Validate Applicant Info, Decision Table | Employment status: FULL_TIME, PART_TIME, SELF_EMPLOYED, UNEMPLOYED, RETIRED |
| `validationPassed` | Boolean | Validate Applicant Info (Worker) | Validation Gateway, Decision Table | Indicates whether applicant information passed validation checks (true/false) |
| `validationErrors` | String | Validate Applicant Info (Worker) | Manual Review, End Event | Comma-separated list of validation errors encountered, if any |
| `creditScore` | Integer | Assess Credit (REST Connector) | Decision Table, Manual Review | Numeric credit score from credit assessment API (300-850 range) |
| `creditStatus` | String | Assess Credit (REST Connector) | Decision Table, Manual Review | Credit status category: Good, Fair, Poor |
| `loanDecision` | String | Loan Decision DMN Table | Decision Gateway, Manual Review, End Event | Final loan decision: Approved, Rejected, Manual Review |
| `reviewNotes` | String | Manual Review (User Task) | End Event | Notes entered by reviewer during manual review process |
| `debtToIncomeRatio` | Double | Validate Applicant Info (Worker) | Decision Table | Calculated ratio of requested loan payment to monthly income |
| `assessmentDate` | String | Assess Credit (REST Connector) | Manual Review | ISO 8601 timestamp of credit assessment |
| `recommendation` | String | Assess Credit (REST Connector) | Decision Table | Credit API recommendation: APPROVE, MANUAL_REVIEW, REJECT |

## Variable Flow Diagram

```
Start Event
  ↓ (applicantId, applicantName, requestedAmount, monthlyIncome, employmentStatus)
Validate Applicant Info Worker
  ↓ (+ validationPassed, validationErrors, debtToIncomeRatio)
Validation Gateway
  ↓ (if validationPassed = true)
Assess Credit REST Connector
  ↓ (+ creditScore, creditStatus, assessmentDate, recommendation)
Loan Decision DMN Table
  ↓ (+ loanDecision)
Decision Gateway
  ↓ (routes based on loanDecision)
  ├─ Approved → End Event (Approved)
  ├─ Rejected → End Event (Rejected)
  └─ Manual Review → User Task (+ reviewNotes) → End Event
```

## Variable Initialization Notes

- All input variables are provided at process start via the Camunda API or start form
- Worker-generated variables are set during task completion using the job client
- REST Connector variables are mapped from the API response body
- DMN output variables are automatically set after decision evaluation
- User task form fields map to process variables on task completion

## Variable Scope

All variables listed above are **process instance variables** with global scope, accessible throughout the entire process instance lifecycle.
