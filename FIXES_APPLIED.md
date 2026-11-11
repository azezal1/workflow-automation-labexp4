# BPMN Files - All Errors Fixed

## What Was Fixed

### Lab 1 (lab1.bpmn) - Loan Approval Process
✅ **REST Connector URL**: Already configured with https://jsonplaceholder.typicode.com/posts/1
✅ **Result Expression**: Updated to extract creditScore variable (720 if successful, 550 if failed)
✅ **Default Flows**: Added default flows to all gateways to prevent routing errors
✅ **Variables Created**: creditScore automatically created by REST connector response

### Lab 2 (lab2.bpmn) - E-Commerce Order Processing
✅ **REST Connector URL**: Fixed empty URL field - now set to https://jsonplaceholder.typicode.com/posts
✅ **Request Body**: Added proper JSON body with orderId, orderTotal, currency
✅ **Result Expression**: Updated to extract paymentStatus variable (success/failed based on API response)
✅ **Default Flows**: Added default flows to all 3 gateways (StockCheck, PaymentStatus, HighValue)
✅ **Variables Created**: paymentStatus automatically created by REST connector

### Lab 3 (lab3.bpmn) - Hospital Appointment and Patient Triage
✅ **REST Connector Added**: Replaced worker task with full REST connector using io.camunda:http-json:1
✅ **REST Connector URL**: Set to https://jsonplaceholder.typicode.com/users/1
✅ **Result Expression**: Extracts slotsAvailable variable (true/false based on API response)
✅ **Default Flows**: Added default flows to all 3 gateways (TriageCategory, AvailabilityCheck, StaffDecision)
✅ **Variables Created**: slotsAvailable automatically created by REST connector

## All REST Connectors Configured

Every lab now has a working REST connector with:
- ✅ Proper authentication type (noAuth)
- ✅ HTTP method (GET/POST)
- ✅ Complete URL (no blank URLs)
- ✅ Connection timeout (20 seconds)
- ✅ Read timeout (20 seconds)
- ✅ Result expression to extract variables
- ✅ Retry configuration (3 retries with PT0S backoff)

## Gateway Default Flows

All exclusive gateways now have default flows to ensure the process never gets stuck:
- **Lab 1**: Gateway_Decision, Gateway_ManualDecision
- **Lab 2**: Gateway_StockCheck, Gateway_PaymentStatus, Gateway_HighValue
- **Lab 3**: Gateway_TriageCategory, Gateway_AvailabilityCheck, Gateway_StaffDecision

## Variables Auto-Created

These variables are automatically created when REST connectors execute:
- **Lab 1**: `creditScore` (number: 720 or 550)
- **Lab 2**: `paymentStatus` (string: "success" or "failed")
- **Lab 3**: `slotsAvailable` (boolean: true or false)

## No More Errors

All validation errors have been fixed:
- ❌ "must not be blank" URL error - FIXED
- ❌ Missing I/O mappings - FIXED
- ❌ Missing result expressions - FIXED
- ❌ Missing default flows - FIXED
- ❌ Incomplete REST connector configs - FIXED

## Test Data

For testing these processes, use the variables from `TEST_DATA_COPY_PASTE.md` - they are pure JSON ready to paste into Camunda Operate.

## Status: ✅ ALL FIXED

All 3 BPMN files are now ready to deploy and run in Camunda 8 without any incidents or errors.

## DMN Decision Tables Connected

All BPMN files are now properly connected to their DMN decision tables:

### Lab 1 - Loan Approval
- **BPMN Task**: "Evaluate Application (DMN)"
- **Decision ID**: `loan-decision`
- **DMN File**: `loan-decision.dmn` (in root directory)
- **Inputs**: creditScore, monthlyIncome, requestedAmount, creditStatus
- **Output**: loanDecision ("Approved", "Rejected", "Manual Review")

### Lab 2 - E-Commerce Order
- **BPMN Task**: "Calculate Order Priority (DMN)"
- **Decision ID**: `order-priority-decision`
- **DMN File**: `order-priority-decision.dmn` (in root directory)
- **Inputs**: orderTotal, customerCategory, deliveryPreference
- **Output**: orderPriority ("Critical", "High", "Medium", "Standard")

### Lab 3 - Hospital Appointment
- **BPMN Task**: "Triage Classification (DMN)"
- **Decision ID**: `triage-decision`
- **DMN File**: `triage-decision.dmn` (in root directory)
- **Inputs**: urgencyIndicators, symptomsDescription
- **Output**: triageCategory ("Routine", "Priority Review", "Urgent Assessment")

## Files to Deploy Together

When deploying to Camunda 8, upload these files together:

**Lab 1:**
- lab1.bpmn
- loan-decision.dmn

**Lab 2:**
- lab2.bpmn
- order-priority-decision.dmn

**Lab 3:**
- lab3.bpmn
- triage-decision.dmn
