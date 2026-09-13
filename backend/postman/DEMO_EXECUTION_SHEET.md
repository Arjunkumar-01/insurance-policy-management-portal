# IPMP API Demo Execution Sheet

## Objective
Demonstrate end-to-end functionality for Auth, Customer, Product, Policy, Claim, Payment, and Report modules using seeded PostgreSQL data and Postman.

## Pre-demo setup
1. Start PostgreSQL and verify database `insurance_portal` exists.
2. Ensure migrations are applied (via app startup/Flyway):
   - `src/main/resources/db/migration/postgresql/V5__policy_role_management.sql`
   - `src/main/resources/db/migration/postgresql/V6__claims_lifecycle_production.sql`
   - `src/main/resources/db/migration/postgresql/V7__payment_management_production.sql`
3. Run seed script: `db/postgres/01_seed_manual_test_data.sql`.
4. Start backend application.
5. Import Postman files:
   - `postman/IPMP-Auth-Customer.postman_collection.json`
   - `postman/IPMP-local.postman_environment.json`
6. Select environment `IPMP Local`.
7. Confirm environment variables include: `adminToken`, `officerToken`, `agentToken`, `customerToken`, `productId`, `policyId`, `policyNumber`, `paymentId`, `paymentNumber`, `claimId`, `claimNumber`, `claimStatus`.

## Demo run order and expected outcomes

### Auth and profile
1. `01 - Login Admin` -> **200** (stores `adminToken`)
2. `02 - Login Claims Officer` -> **200** (stores `officerToken`)
3. `03 - Register Customer` -> **201**
4. `04 - Login Customer` -> **200** (stores `customerToken`)
5. `05 - Get My Profile` -> **200**
6. `06 - Update My Profile` -> **200**

### Product
7. `07 - Create Product (Admin)` -> **201** (stores `productId`)
8. `08 - Search Products` -> **200** (stores `seededProductId`)
9. `09 - Compare Products` -> **200**

### Policy
10. `10 - Purchase Policy (Customer)` -> **201** (stores `policyId`, `policyNumber`)
11. `11 - Get My Policies` -> **200**
12. `GET /api/policies/{{policyId}}` -> **200**
13. `GET /api/policies/{{policyId}}/coverage` -> **200**
14. `POST /api/policies/{{policyId}}/renew` -> **200**

Legacy compatibility (optional): `POST /api/policies/purchase` and `/api/policies/by-number/{policyNumber}/...` remain supported.

### Claims
15. `13 - Submit Claim` -> **201** (stores `claimId`, `claimNumber`)
16. `14 - Claims Queue (Officer)` -> **200**
17. `15 - Review Claim (Officer)` -> **200**
18. `15.1 - Approve Claim (Officer)` -> **200**

### Payments
19. `16 - Make Payment` -> **201**
20. `17 - Payment History` -> **200**
21. `GET /api/payments/{{paymentId}}` -> **200**
22. `GET /api/payments/{{paymentId}}/receipt` -> **200**

### Policy cancellation flow
23. `POST /api/policies/{{policyId}}/cancel` -> **200**
24. `PATCH /api/policies/{{policyId}}/approve-cancellation` -> **200**
25. `PATCH /api/policies/{{policyId}}/status` -> **200**

### Reports
26. `20 - Customer Report` -> **200**
27. `21 - Admin Report` -> **200**
28. `GET /api/payments/analytics` -> **200**
29. `GET /api/payments/revenue` -> **200**

### Customer admin management
30. `22 - Create Customer (Admin)` -> **201** (stores `createdCustomerId`)
31. `23 - Get Customer By ID (Admin)` -> **200**
32. `24 - Delete Customer By ID (Admin)` -> **200**

### Agent and role-based policy checks
33. `GET /api/policies?query={{policyNumber}}` with `agentToken` -> **200**
34. `GET /api/policies/customer/{{createdCustomerId}}?page=0&size=20` with `agentToken` -> **200**
35. `GET /api/policies/{{policyId}}/coverage` with `officerToken` -> **200**
36. `GET /api/payments/customer/{{createdCustomerId}}?page=0&size=20` with `agentToken` -> **200**

## Suggested negative checks (quick)
- Use customer token on admin API (`07` or `22`) -> **403**
- Remove token on protected API (`05`) -> **401**
- Re-run `03` without cleanup -> **400** duplicate username/email
- Use `officerToken` on `POST /api/policies/{{policyId}}/renew` -> **403**
- Use customer token to fetch another customer's policies `GET /api/policies/customer/{otherId}` -> **403**
- Use customer token on `GET /api/payments/revenue` -> **403**

## Post-demo cleanup
Run: `db/postgres/02_cleanup_manual_test_data.sql`

## Notes for evaluator
- JWT auth + role-based access are validated through role-specific endpoints.
- API wrapper uses `data` as response payload key.
- Policy responses include `policyStatus`, `renewalStatus`, `cancellationStatus`, and `coverageAmount`.

