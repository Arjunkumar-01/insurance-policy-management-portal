# Complete Manual Testing Guide (Postman + PostgreSQL)

This guide contains the full test flow from **Step 1 to 22** and then additional steps for the latest enhancements.

## Global Notes
- Base URL: `http://localhost:8080`
- API response payload key is `data`.
- Public registration creates `CUSTOMER` by default; `ADMIN` / `CLAIMS_OFFICER` / `AGENT` must be role-promoted in DB for bootstrap.
- Product model now includes `productCode`, `description`, `status`, `createdAt`, and `updatedAt`.
- Policy model now includes `coverageAmount`, `renewalStatus`, `cancellationStatus`, `policyStatus`, `createdAt`, and `updatedAt`.
- Claim model now includes `claimAmount`, `submittedDate`, `reviewedDate`, and `settledDate`.
- Payment model now includes `paymentMethod`, `paymentStatus`, `transactionReference`, and `failureReason`.
- Migrations `V5__policy_role_management.sql`, `V6__claims_lifecycle_production.sql`, and `V7__payment_management_production.sql` must be applied before executing tests.

## Recommended Postman Environment Variables
- `baseUrl = http://localhost:8080`
- `customerToken`
- `adminToken`
- `officerToken`
- `agentToken`
- `productId1`
- `productId2`
- `policyId`
- `policyNumber`
- `paymentId`
- `paymentNumber`
- `claimId`
- `claimNumber`
- `claimStatus`
- `managedCustomerId`

---

## Steps 1 to 22 (Core Full Flow)

### 1) Ensure DB exists
```sql
CREATE DATABASE insurance_portal;
```

### 2) Start application
```powershell
.\mvnw.cmd spring-boot:run
```

### 3) Create Postman environment variables
Create the variables listed in "Recommended Postman Environment Variables".

### 4) Register customer user
`POST {{baseUrl}}/api/auth/register`
```json
{
  "firstName": "John",
  "lastName": "Customer",
  "dateOfBirth": "1995-05-10",
  "gender": "MALE",
  "phoneNumber": "9876543210",
  "email": "john.customer@example.com",
  "username": "john_customer",
  "password": "Password@1",
  "confirmPassword": "Password@1",
  "addressLine1": "Street 1",
  "addressLine2": "Apt 1",
  "city": "Boston",
  "state": "MA",
  "country": "USA",
  "postalCode": "02110"
}
```

### 5) Register future admin
`POST {{baseUrl}}/api/auth/register`
```json
{
  "firstName": "Alice",
  "lastName": "Admin",
  "dateOfBirth": "1990-01-01",
  "gender": "FEMALE",
  "phoneNumber": "9876543211",
  "email": "alice.admin@example.com",
  "username": "alice_admin",
  "password": "Password@1",
  "confirmPassword": "Password@1",
  "addressLine1": "Admin Street",
  "addressLine2": "Suite 1",
  "city": "Boston",
  "state": "MA",
  "country": "USA",
  "postalCode": "02111"
}
```

### 6) Register future claims officer
`POST {{baseUrl}}/api/auth/register`
```json
{
  "firstName": "Clara",
  "lastName": "Officer",
  "dateOfBirth": "1991-02-02",
  "gender": "FEMALE",
  "phoneNumber": "9876543212",
  "email": "clara.officer@example.com",
  "username": "clara_officer",
  "password": "Password@1",
  "confirmPassword": "Password@1",
  "addressLine1": "Officer Street",
  "addressLine2": "Floor 2",
  "city": "Boston",
  "state": "MA",
  "country": "USA",
  "postalCode": "02112"
}
```

### 7) Register future agent
`POST {{baseUrl}}/api/auth/register`
```json
{
  "firstName": "Aaron",
  "lastName": "Agent",
  "dateOfBirth": "1992-03-03",
  "gender": "MALE",
  "phoneNumber": "9876543213",
  "email": "aaron.agent@example.com",
  "username": "aaron_agent",
  "password": "Password@1",
  "confirmPassword": "Password@1",
  "addressLine1": "Agent Street",
  "addressLine2": "Floor 3",
  "city": "Boston",
  "state": "MA",
  "country": "USA",
  "postalCode": "02113"
}
```

### 8) Promote roles in DB (required bootstrap)
```sql
UPDATE customers SET role = 'ADMIN' WHERE username = 'alice_admin';
UPDATE customers SET role = 'CLAIMS_OFFICER' WHERE username = 'clara_officer';
UPDATE customers SET role = 'AGENT' WHERE username = 'aaron_agent';

SELECT id, username, role, enabled FROM customers ORDER BY id;
```

### 9) Login customer
`POST {{baseUrl}}/api/auth/login`
```json
{ "username": "john_customer", "password": "Password@1" }
```
Save `data.accessToken` -> `customerToken`.

### 10) Login admin
`POST {{baseUrl}}/api/auth/login`
```json
{ "username": "alice_admin", "password": "Password@1" }
```
Save `data.accessToken` -> `adminToken`.

### 11) Login claims officer
`POST {{baseUrl}}/api/auth/login`
```json
{ "username": "clara_officer", "password": "Password@1" }
```
Save `data.accessToken` -> `officerToken`.

### 12) Login agent
`POST {{baseUrl}}/api/auth/login`
```json
{ "username": "aaron_agent", "password": "Password@1" }
```
Save `data.accessToken` -> `agentToken`.

### 13) Customer profile fetch
`GET {{baseUrl}}/api/customers/me` with `Bearer {{customerToken}}`.

### 14) Customer profile update
`PUT {{baseUrl}}/api/customers/me`
```json
{
  "firstName": "John",
  "lastName": "CustomerUpdated",
  "phoneNumber": "9998887776",
  "email": "john.customer.updated@example.com",
  "addressLine1": "New Address",
  "addressLine2": "Apt 10",
  "city": "Boston",
  "state": "MA",
  "country": "USA",
  "postalCode": "02120"
}
```

### 15) Create product #1 (admin)
`POST {{baseUrl}}/api/products`
```json
{
  "productCode": "PRD-TRAVEL-001",
  "productName": "Travel Shield Plus",
  "productCategory": "TRAVEL",
  "description": "Travel coverage for medical and trip disruptions",
  "coverageAmount": 500000.00,
  "premiumAmount": 12000.00,
  "policyTenureMonths": 12
}
```
Save `data.id` -> `productId1`.

### 16) Create product #2 (admin)
`POST {{baseUrl}}/api/products`
```json
{
  "productCode": "PRD-HEALTH-001",
  "productName": "Health Secure Basic",
  "productCategory": "HEALTH",
  "description": "Health plan with hospitalization coverage",
  "coverageAmount": 300000.00,
  "premiumAmount": 9000.00,
  "policyTenureMonths": 12
}
```
Save `data.id` -> `productId2`.

### 17) Search + compare products
- `GET {{baseUrl}}/api/products`
- `GET {{baseUrl}}/api/products/{{productId1}}`
- `GET {{baseUrl}}/api/products/search?category=TRAVEL`
- `GET {{baseUrl}}/api/products/search?keyword=Shield`
- `GET {{baseUrl}}/api/products/compare?ids={{productId1}}&ids={{productId2}}`
- `GET {{baseUrl}}/api/products/recommendations?category=TRAVEL&limit=3` (customer or agent)

### 18) Purchase policy (customer)
`POST {{baseUrl}}/api/policies`
```json
{
  "productId": {{productId1}},
  "nomineeName": "Jane Nominee",
  "nomineeRelation": "SPOUSE"
}
```
Save `data.id` -> `policyId`.
Save `data.policyNumber` -> `policyNumber`.

Note: legacy alias `POST {{baseUrl}}/api/policies/purchase` is still supported.

### 19) Policy self operations
- `GET {{baseUrl}}/api/policies/me`
- `GET {{baseUrl}}/api/policies/{{policyId}}`
- `GET {{baseUrl}}/api/policies/{{policyId}}/coverage`
- `POST {{baseUrl}}/api/policies/{{policyId}}/renew`
- `POST {{baseUrl}}/api/policies/{{policyId}}/cancel`

Verify in responses:
- `data.policyStatus`
- `data.renewalStatus`
- `data.cancellationStatus`
- `data.coverageAmount`

### 20) Claim flow
`POST {{baseUrl}}/api/claims`
```json
{
  "policyNumber": "{{policyNumber}}",
  "incidentDate": "2026-08-01",
  "claimAmount": 12000.00,
  "description": "Minor accident with estimate attached",
  "supportingDocuments": "https://example.com/docs/claim-1.pdf"
}
```
Save `data.id` -> `claimId`.
Save `data.claimNumber` -> `claimNumber`.

Then:
- `GET {{baseUrl}}/api/claims/queue` (`officerToken`)
- `PATCH {{baseUrl}}/api/claims/{{claimId}}/review` (`officerToken`) with optional body:
```json
{ "decisionReason": "Initial review started." }
```
- `PATCH {{baseUrl}}/api/claims/{{claimId}}/approve` (`officerToken`) with optional body:
```json
{ "decisionReason": "Verified supporting documents and policy validity." }
```
- `GET {{baseUrl}}/api/claims/{{claimId}}/status`
- `GET {{baseUrl}}/api/claims/my` (`customerToken`)
- `GET {{baseUrl}}/api/claims/me` (`customerToken`, legacy alias)

### 21) Payment flow
`POST {{baseUrl}}/api/payments`
```json
{
  "policyNumber": "{{policyNumber}}",
  "amount": 12000.00,
  "paymentMethod": "UPI"
}
```
Then:
- `GET {{baseUrl}}/api/payments/my`
- `GET {{baseUrl}}/api/payments/history` (legacy alias)
- `GET {{baseUrl}}/api/payments/{{paymentId}}` (save `data.id` from create response as `paymentId`)
- `GET {{baseUrl}}/api/payments/{{paymentId}}/receipt`

Verify payment response keys:
- `data.paymentStatus` should be `SUCCESSFUL`
- `data.paymentMethod` should match request
- `data.transactionReference` should be non-null

### 22) Reports + admin/customer management
- `PATCH {{baseUrl}}/api/policies/{{policyId}}/approve-cancellation` (`adminToken`)
- `PATCH {{baseUrl}}/api/policies/{{policyId}}/status` (`adminToken`)
```json
{ "policyStatus": "ACTIVE" }
```
- `GET {{baseUrl}}/api/reports/customer` (`customerToken`)
- `GET {{baseUrl}}/api/reports/admin` (`adminToken`)
- `GET {{baseUrl}}/api/admin/overview` (`adminToken`)
- `POST {{baseUrl}}/api/customers` (`adminToken`) create managed customer, save `data.id` -> `managedCustomerId`
- `GET {{baseUrl}}/api/customers/{{managedCustomerId}}`
- `DELETE {{baseUrl}}/api/customers/{{managedCustomerId}}`

---

## Additional Steps for Latest Enhancements

### 23) Admin customer list/search/filter (new)
Run with `adminToken`:
- `GET {{baseUrl}}/api/customers?page=0&size=20`
- `GET {{baseUrl}}/api/customers?query=john&page=0&size=20`
- `GET {{baseUrl}}/api/customers?role=CUSTOMER&enabled=true&page=0&size=20`

### 24) Admin policy list + cancellation discovery (new)
Run with `adminToken`:
- `GET {{baseUrl}}/api/policies?page=0&size=20`
- `GET {{baseUrl}}/api/policies?query={{policyNumber}}`
- `GET {{baseUrl}}/api/policies?status=CANCEL_REQUESTED`
- `GET {{baseUrl}}/api/policies/{{policyId}}`
- `GET {{baseUrl}}/api/policies/pending-cancellations?page=0&size=20`

### 25) Agent APIs (new)
Run with `agentToken`:
- `GET {{baseUrl}}/api/agents/customers?query=john&page=0&size=20`
- `GET {{baseUrl}}/api/agents/customers/john_customer/policies`
- `GET {{baseUrl}}/api/policies?page=0&size=20`
- `GET {{baseUrl}}/api/policies/customer/{{managedCustomerId}}?page=0&size=20`

Agent assisted purchase (new):
`POST {{baseUrl}}/api/policies`
```json
{
  "customerId": {{managedCustomerId}},
  "productId": {{productId1}},
  "nomineeName": "Managed Nominee",
  "nomineeRelation": "BROTHER"
}
```

### 26) Claim decision reason validation (new)
- Approve/reject with empty reason should fail:
`PATCH {{baseUrl}}/api/claims/{{claimId}}/reject`
```json
{ "decisionReason": "" }
```
Expect `400`.

### 27) Claim external URL validation (new)
`POST /api/claims` with invalid URL:
```json
{
  "policyNumber": "{{policyNumber}}",
  "incidentDate": "2026-08-01",
  "claimAmount": 5000.00,
  "description": "Bad URL test",
  "supportingDocuments": "ftp://example.com/file.pdf"
}
```
Expect `400`.

### 27.1) Claim eligibility validation (new)
- Cancel or expire a policy, then submit claim for that policy.
- `POST {{baseUrl}}/api/claims` with same payload structure as Step 20.
- Expect `400` with coverage period/status validation message.

### 28) Typed admin report payload check (new)
`GET {{baseUrl}}/api/reports/admin` and verify:
- `data.productPerformance[]` (`productName`, `policyCount`)
- `data.monthlyRevenue[]` (`month`, `revenue`)

### 28.1) Payment analytics and revenue (new)
Run with `adminToken`:
- `GET {{baseUrl}}/api/payments/analytics`
- `GET {{baseUrl}}/api/payments/revenue`
- `GET {{baseUrl}}/api/payments?page=0&size=20`

Verify analytics includes:
- `totalPayments`, `pendingPayments`, `processingPayments`, `successfulPayments`, `failedPayments`, `refundedPayments`
- `totalPremiumCollected`, `successRate`, `failureRate`

### 28.2) Payment status transition validations (new)
Run with `adminToken`:
- Invalid transition example (expect `400`): set a successful payment back to pending
`PATCH {{baseUrl}}/api/payments/{{paymentId}}/status`
```json
{ "paymentStatus": "PENDING" }
```
- Failed payment requires reason (expect `400` without reason)
`PATCH {{baseUrl}}/api/payments/{{paymentId}}/status`
```json
{ "paymentStatus": "FAILED" }
```

### 29) Security/RBAC checks (new)
- customer token on `/api/admin/overview` -> `403`
- agent token on `POST /api/products` -> `403`
- missing token on `/api/customers/me` -> `401`
- claims officer token on `POST /api/policies/{{policyId}}/renew` -> `403`
- claims officer token on `GET /api/policies/{{policyId}}/coverage` -> `200`
- customer token on `GET /api/policies/customer/{{managedCustomerId}}` (other customer) -> `403`
- agent token on `GET /api/payments/customer/{{managedCustomerId}}` -> `200`
- customer token on `GET /api/payments/{{paymentId}}` of another customer -> `403`

### 30) Product admin lifecycle APIs (new)
Run with `adminToken`:

- Update product
`PUT {{baseUrl}}/api/products/{{productId1}}`
```json
{
  "productCode": "PRD-TRAVEL-001",
  "productName": "Travel Shield Plus - Updated",
  "productCategory": "TRAVEL",
  "description": "Updated travel coverage details",
  "coverageAmount": 550000.00,
  "premiumAmount": 12500.00,
  "policyTenureMonths": 12
}
```

- Deactivate product
`PATCH {{baseUrl}}/api/products/{{productId1}}/deactivate`

- Verify inactive product is not purchasable
- `POST {{baseUrl}}/api/policies` as customer
```json
{
  "productId": {{productId1}},
  "nomineeName": "Check Inactive",
  "nomineeRelation": "SPOUSE"
}
```
Expect `400` with inactive product message.

- Activate product
`PATCH {{baseUrl}}/api/products/{{productId1}}/activate`

- Delete product (only when not referenced by active business flow)
`DELETE {{baseUrl}}/api/products/{{productId2}}`

### 31) Product role-access validation matrix
- `CUSTOMER` can call: `GET /api/products`, `GET /api/products/{id}`, `GET /api/products/compare`, `GET /api/products/recommendations`.
- `AGENT` can call: `GET /api/products`, `GET /api/products/{id}`, `GET /api/products/compare`, `GET /api/products/recommendations`.
- `CLAIMS_OFFICER` can call: `GET /api/products`, `GET /api/products/{id}`, `GET /api/products/compare`.
- `CLAIMS_OFFICER` cannot call: `GET /api/products/recommendations` -> expect `403`.
- `ADMIN` can call all product endpoints including create/update/delete/activate/deactivate.

---

## Sample DB Verification Queries

```sql
SELECT id, username, email, role, enabled FROM customers ORDER BY id;
SELECT id, product_code, product_name, product_category, status, premium_amount, created_at, updated_at FROM products ORDER BY id;
SELECT id, policy_number, customer_id, product_id, coverage_amount, renewal_premium, status, renewal_status, cancellation_status, created_at, updated_at FROM policies ORDER BY id;
SELECT id, claim_number, policy_id, submitted_by_customer_id, claim_amount, status, submitted_date, reviewed_date, settled_date, decision_reason FROM claims ORDER BY id;
SELECT id, payment_reference, customer_id, policy_id, amount, payment_method, status, transaction_reference, failure_reason, payment_date FROM payments ORDER BY id;
```

---

## Optional Reset (only if this DB is dedicated to testing)

```sql
DELETE FROM payments;
DELETE FROM claims;
DELETE FROM policies;
DELETE FROM products;
DELETE FROM customers;
```

