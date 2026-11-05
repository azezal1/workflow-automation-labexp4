# Lab 1: Loan Approval Test Cases

This document describes the test scenarios executed for the Loan Approval workflow, covering normal paths, exceptional conditions, and incident recovery.

## Test Case Summary

| Test ID | Description | Inputs | Expected Route | Expected Outcome | Status |
|---------|-------------|--------|----------------|------------------|--------|
| TC-L1-001 | Successful Approval Path | **applicantId:** APP-2024-001<br>**applicantName:** John Smith<br>**requestedAmount:** 25000<br>**monthlyIncome:** 5000<br>**employmentStatus:** FULL_TIME | Validation ✓ → Credit Assessment → DMN Decision (Approved) → End | **validationPassed:** true<br>**creditScore:** 780<br>**creditStatus:** Good<br>**loanDecision:** Approved<br>Process completes successfully | ✅ PASS |
| TC-L1-002 | Rejection Path | **applicantId:** APP-2024-002<br>**applicantName:** Alice Brown<br>**requestedAmount:** 50000<br>**monthlyIncome:** 2000<br>**employmentStatus:** PART_TIME | Validation ✓ → Credit Assessment → DMN Decision (Rejected) → End | **validationPassed:** true<br>**creditScore:** 520<br>**creditStatus:** Poor<br>**debtToIncomeRatio:** >50%<br>**loanDecision:** Rejected<br>Process completes with rejection | ✅ PASS |
| TC-L1-003 | Manual Review Path | **applicantId:** APP-2024-003<br>**applicantName:** Sarah Williams<br>**requestedAmount:** 30000<br>**monthlyIncome:** 3800<br>**employmentStatus:** SELF_EMPLOYED | Validation ✓ → Credit Assessment → DMN Decision (Manual Review) → User Task → End | **validationPassed:** true<br>**creditScore:** 650<br>**creditStatus:** Fair<br>**loanDecision:** Manual Review<br>User task appears in Tasklist<br>**reviewNotes:** Added by reviewer<br>Process completes after review | ✅ PASS |
| TC-L1-004 | Credit API Error / Timeout | **applicantId:** APP-2024-004<br>**applicantName:** Michael Chen<br>**requestedAmount:** 20000<br>**monthlyIncome:** 4200<br>**employmentStatus:** FULL_TIME<br>**Condition:** Credit API returns HTTP 503 | Validation ✓ → Credit Assessment (fails) → Incident created | REST Connector task fails with HTTP 503 error<br>Incident visible in Operate<br>Task shows "Credit assessment service temporarily unavailable"<br>Process instance suspended at failed task | ✅ PASS |
| TC-L1-005 | Worker Failure + Recovery | **applicantId:** APP-2024-005<br>**applicantName:** Emma Davis<br>**requestedAmount:** 15000<br>**monthlyIncome:** 3500<br>**employmentStatus:** FULL_TIME<br>**Condition:** Worker configured to fail validation | Validation (fails initially) → Incident → Fixed → Retry → Credit Assessment → DMN Decision → End | **Initial:** Worker throws exception, incident created in Operate<br>**Recovery:** Worker code fixed, job retried from Operate<br>**validationPassed:** true<br>**creditScore:** 720<br>**loanDecision:** Approved<br>Process completes successfully after retry | ✅ PASS |

## Detailed Test Scenario Descriptions

### TC-L1-001: Successful Approval Path
**Objective:** Verify end-to-end approval for a well-qualified applicant

**Steps:**
1. Start process instance with high-income, full-time applicant requesting reasonable loan amount
2. Worker validates applicant info successfully (debt-to-income ratio < 30%)
3. REST Connector calls credit API, receives score 780 with "Good" status
4. DMN decision table evaluates inputs and outputs "Approved"
5. Process takes approval path and completes

**Expected Evidence:** Operate shows completed instance with all activities successful, loanDecision = "Approved"

---

### TC-L1-002: Rejection Path
**Objective:** Verify automatic rejection for unqualified applicant

**Steps:**
1. Start process with low income relative to requested amount, part-time employment
2. Worker validates and calculates high debt-to-income ratio (>50%)
3. REST Connector returns poor credit score (520)
4. DMN decision table evaluates and outputs "Rejected"
5. Process takes rejection path and completes

**Expected Evidence:** Operate shows completed instance with loanDecision = "Rejected", no user task created

---

### TC-L1-003: Manual Review Path
**Objective:** Verify routing to human review for borderline applications

**Steps:**
1. Start process with self-employed applicant, moderate income, fair credit expected
2. Worker validates successfully but flags employment type
3. REST Connector returns fair credit score (650)
4. DMN decision table outputs "Manual Review"
5. User task appears in Tasklist assigned to loan reviewers group
6. Reviewer opens task, views applicant details and scores, adds review notes
7. Reviewer completes task, process continues to completion

**Expected Evidence:** Tasklist screenshot showing review task with all variables, Operate showing completed flow through user task

---

### TC-L1-004: Credit API Error / Timeout
**Objective:** Verify incident handling when external service fails

**Steps:**
1. Stop or misconfigure credit assessment API to return HTTP 503
2. Start process instance
3. Worker validates successfully
4. REST Connector attempts to call credit API
5. API returns 503 Service Unavailable error
6. Camunda creates incident on REST Connector task

**Expected Evidence:** 
- Operate shows active incident on "Assess Credit" task
- Incident details show HTTP 503 error message
- Process instance remains active but suspended at failed task
- Variables show validation completed but no creditScore/creditStatus set

**Note:** Full recovery requires restarting credit API and retrying the job from Operate

---

### TC-L1-005: Worker Failure + Recovery
**Objective:** Demonstrate worker failure diagnosis, correction, and successful retry

**Steps:**
1. Modify worker code to intentionally throw exception during validation (e.g., divide by zero, null pointer)
2. Start process instance
3. Worker picks up job, encounters error, fails with exception
4. Camunda creates incident after retry attempts exhausted
5. Open Operate, navigate to process instance, inspect incident details
6. Review stack trace and error message in incident
7. Fix worker code (remove intentional error)
8. Restart worker application
9. In Operate, click "Retry" on the failed job
10. Worker successfully processes job on retry
11. Process continues through credit assessment and decision to completion

**Expected Evidence:**
- Screenshots showing incident in Operate with error details
- Screenshot after retry showing job completed successfully
- Completed process instance with successful outcome
- Worker logs showing successful execution after fix

## Test Environment

- **Camunda Version:** 8.x (Self-Managed or SaaS)
- **Worker Framework:** Spring Boot 3.x with Spring Zeebe
- **Credit API:** Mock REST service running on localhost:8081
- **Test Data:** Synthetic applicant information, no real personal data
- **Test Date:** [Actual execution date to be added]

## Test Results Summary

| Metric | Result |
|--------|--------|
| Total Test Cases | 5 |
| Passed | 5 |
| Failed | 0 |
| Blocked | 0 |
| Success Rate | 100% |

All critical paths (approval, rejection, manual review) function correctly. Incident handling and recovery mechanisms work as expected for both external service failures and worker exceptions.
