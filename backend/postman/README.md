# IPMP Manual Testing Pack

## Files
- `postman/IPMP-Auth-Customer.postman_collection.json`
- `postman/IPMP-local.postman_environment.json`
- `postman/COMPLETE_MANUAL_TESTING_GUIDE.md`
- `postman/DEMO_EXECUTION_SHEET.md`
- `db/postgres/01_seed_manual_test_data.sql`
- `db/postgres/02_cleanup_manual_test_data.sql`

## Seed data users
- `admin_manual / Password@1`
- `officer_manual / Password@1`
- `agent_manual / Password@1`
- `customer_manual / Password@1`

## Step-by-step usage
1. Start PostgreSQL and ensure `insurance_portal` database exists.
2. Ensure migrations are applied:
   - `src/main/resources/db/migration/postgresql/V5__policy_role_management.sql`
   - `src/main/resources/db/migration/postgresql/V6__claims_lifecycle_production.sql`
   - `src/main/resources/db/migration/postgresql/V7__payment_management_production.sql`
3. Run seed script `01_seed_manual_test_data.sql`.
4. Start backend service.
5. Import both Postman files.
6. Select environment `IPMP Local`.
7. Execute requests in collection order (`01` to `24`) and run extended policy RBAC checks from `postman/COMPLETE_MANUAL_TESTING_GUIDE.md`.
8. Run cleanup script `02_cleanup_manual_test_data.sql` when done.

## Notes
- Response payload key is `data` in this project.
- Dynamic values are auto-saved into environment variables (`adminToken`, `officerToken`, `agentToken`, `customerToken`, `productId`, `policyId`, `policyNumber`, `paymentId`, `paymentNumber`, `claimId`, `claimNumber`, `claimStatus`, `createdCustomerId`).
- If `03 - Register Customer` fails due to duplicate username/email, run cleanup script and retry.
- Preferred policy APIs are ID-based (`/api/policies/{id}`); legacy alias `/api/policies/purchase` and by-number policy operations are still supported.
- Payment create requests must include `paymentMethod` (e.g., `UPI`, `CREDIT_CARD`, `DEBIT_CARD`, `NET_BANKING`, `INSURANCE_WALLET`).
- Claim create requests must include `claimAmount` and use lifecycle transitions `SUBMITTED -> UNDER_REVIEW -> APPROVED -> SETTLED`.

