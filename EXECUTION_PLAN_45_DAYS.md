# Pryme Project — 45-Day Day-by-Day Execution Plan

## Tech Stack (Locked)
- Frontend: Next.js 14 (App Router), TypeScript, Tailwind CSS, Shadcn/UI
- Backend: Java 21, Spring Boot 3.2, Spring Data JPA, Hibernate, Lombok
- Database: PostgreSQL 16
- DevOps: Docker, GitHub Actions, Vercel (Frontend), AWS EC2/Render (Backend)

---

## How to Use This Plan
For every day, track execution in this format:
- **Objective**: What must be achieved today.
- **Build Tasks**: Concrete implementation items.
- **Exit Criteria**: Non-negotiable “done” checks before closing the day.

---

## PHASE 1 — THE IRON FOUNDATION (Days 1–7)
**Goal by Day 7:** Financial math core is implemented and tested in Java.

### Day 1 — Environment & Repository Setup
**Objective**
- Establish reproducible local dev setup and monorepo structure.

**Build Tasks**
- Create monorepo folders: `/frontend` and `/backend`.
- Initialize backend from start.spring.io with Maven.
- Add dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Lombok, Validation, Spring Security, DevTools.
- Initialize frontend with:
  - `npx create-next-app@latest frontend --typescript --tailwind --eslint`
- Install PostgreSQL and create `pryme_db_dev`.

**Exit Criteria**
- Backend app starts successfully.
- Frontend app starts successfully.
- Backend can connect to local `pryme_db_dev`.

### Day 2 — Database Schema Engineering (ERD)
**Objective**
- Finalize core data model and enforce secure keying strategy.

**Build Tasks**
- Design ERD in dbdiagram.io (or equivalent).
- Implement entities with UUID primary keys for:
  - `users(id, email, password_hash, role[ADMIN/USER])`
  - `banks(id, name, logo_url, is_active)`
  - `loan_products(id, bank_id, min_salary, min_cibil, interest_rate, processing_fee, type[HOME/PERSONAL/BUSINESS])`
  - `leads(id, user_name, phone, loan_amount, status[NEW/CONTACTED])`
  - `cms_posts(id, title, content, is_pinned)`
- Add relationships and constraints.

**Exit Criteria**
- Schema migrates cleanly.
- All entities persist and read correctly in local DB.

### Day 3 — Pre-Payment Math Engine (Architecture)
**Objective**
- Lock interfaces and precision rules for all loan math.

**Build Tasks**
- Create `LoanMathService.java` interface/contract.
- Define methods:
  - `calculateAmortization(principal, rate, tenure)`
  - `simulate13thEMIPayment()`
  - `simulateStepUpPayment(percentage)`
- Enforce `BigDecimal` only (never `double`) across calculations.

**Exit Criteria**
- Service contracts compile.
- No floating-point primitive usage in financial calculations.

### Day 4 — Pre-Payment Math Engine (Implementation)
**Objective**
- Deliver month-wise amortization and optimization simulations.

**Build Tasks**
- Implement monthly loop from `1..tenure_in_months`.
- Compute:
  - `interest = outstanding * rate / 1200`
  - `principalComponent = emi - interest`
- Inject extra-payment strategies (13th EMI, step-up) in-loop.
- Return `List<AmortizationSchedule>` with full month-by-month payload.

**Exit Criteria**
- APIs/services return valid schedules for baseline and optimized cases.
- Outstanding balance closes correctly at loan end.

### Day 5 — Unit Testing the Math (Mandatory)
**Objective**
- Prove financial output correctness against reference numbers.

**Build Tasks**
- Add JUnit 5 + Mockito tests in `LoanMathServiceTest.java`.
- Use benchmark case (e.g., ₹50L at 8.5% for 20 years).
- Assert EMI, interest split, and total interest with exact decimal expectations.

**Exit Criteria**
- Test suite passes.
- Java output matches Excel benchmark to exact expected precision.

### Day 6 — Bank Filtering Processor
**Objective**
- Implement fast eligibility-based bank product retrieval.

**Build Tasks**
- Build `BankRecommendationService.java`.
- Implement JPA Specification filters:
  - `min_salary <= input_salary`
  - `min_cibil <= input_cibil`
- Add indexes on `min_salary`, `min_cibil`.

**Exit Criteria**
- Query returns correct products for varied salary/CIBIL combinations.
- Query plan confirms index usage.

### Day 7 — API Contract & Swagger
**Objective**
- Freeze discoverable backend contract for frontend integration.

**Build Tasks**
- Add `springdoc-openapi-ui`.
- Annotate controllers and DTOs.
- Verify Swagger route: `http://localhost:8080/swagger-ui.html`.

**Exit Criteria**
- All active endpoints visible and testable in Swagger UI.

---

## PHASE 2 — BACKEND SECURITY & ADMIN CONTROL (Days 8–14)
**Goal by Day 14:** Secured admin-ready backend.

### Day 8 — Spring Security & JWT
**Objective**
- Enable stateless authentication and role-based access.

**Build Tasks**
- Implement `JwtAuthenticationFilter` and `SecurityFilterChain`.
- Configure routes:
  - Public: `/api/v1/auth/*`, `/api/v1/public/*`
  - Admin-only: `/api/v1/admin/**` (`ROLE_ADMIN`)
- Choose token transport (Bearer header or HTTP-only cookie).

**Exit Criteria**
- Public endpoints accessible without auth.
- Admin endpoints reject non-admin access.

### Day 9 — Admin Bank Management APIs
**Objective**
- Give admins full operational control over banks and pricing.

**Build Tasks**
- Implement `AdminBankController.java` CRUD.
- Add actions: `createBank`, `updateInterestRate`, `toggleBankVisibility`.
- Add validation (`@Valid`) so interest rate cannot be negative.

**Exit Criteria**
- Admin can create/update/toggle records securely.
- Validation errors return clean 4xx responses.

### Day 10 — Lead Management System (CRM)
**Objective**
- Capture public leads and expose paginated admin retrieval.

**Build Tasks**
- Implement `LeadController.java`.
- Add endpoints:
  - `POST /api/v1/public/leads`
  - `GET /api/v1/admin/leads`
- Implement `Pageable` with page size 20.

**Exit Criteria**
- Leads are stored from public form payloads.
- Admin dashboard endpoint paginates correctly.

### Day 11 — CMS Backend (Blogs & Reviews)
**Objective**
- Enable blog publishing with stable URL slugs.

**Build Tasks**
- Create `BlogPost` fields: `title`, `slug(unique)`, `content(TEXT)`, `isPinned`, `createdAt`.
- Implement `BlogController.java`.
- Add duplicate-safe slug logic (`loan-tips`, `loan-tips-1`, ...).

**Exit Criteria**
- Duplicate titles generate unique slugs deterministically.

### Day 12 — Rewards Calculator Backend
**Objective**
- Provide rewards savings computation API.

**Build Tasks**
- Add `CreditCardRewards` configuration table:
  - `card_name`, `dining_multiplier`, `travel_multiplier`, `point_value`
- Add endpoint: `POST /api/v1/calculate/rewards`.
- Compute annual savings from spending breakdown.

**Exit Criteria**
- Endpoint returns deterministic totals for fixed inputs.

### Day 13 — Exception Handling & Logging
**Objective**
- Standardize error contracts and production logs.

**Build Tasks**
- Add global exception handler using `@ControllerAdvice`.
- Return clean 500 payloads (e.g., `{"code":"DB_ERROR","message":"Try again"}`).
- Configure Logback output file at `/var/logs/pryme-app.log`.

**Exit Criteria**
- Unhandled exceptions produce structured JSON.
- Logs written to configured file path.

### Day 14 — CORS & Security Headers
**Objective**
- Restrict cross-origin access and finalize backend core hardening.

**Build Tasks**
- Allow only:
  - `http://localhost:3000`
  - `https://pryme.in`
- Deny all other origins.
- Apply secure response headers and freeze backend core.

**Exit Criteria**
- Unauthorized origins blocked.
- Security headers verified in responses.

---

## PHASE 3 — FRONTEND CORE (Days 15–23)
**Goal by Day 23:** Calculator and form UX is visually functional.

### Day 15 — ShadCN/UI & Design System
**Objective**
- Establish reusable component baseline and brand theming.

**Build Tasks**
- Install ShadCN/UI.
- Add: Button, Input, Slider, Card, Dialog, Table, Form, Toast.
- Configure Gold/Black/White in `tailwind.config.ts`.

**Exit Criteria**
- Core UI components render consistently across pages.

### Day 16 — API Client & State Management
**Objective**
- Standardize data fetching and authenticated request behavior.

**Build Tasks**
- Integrate Axios and TanStack Query.
- Build `useAxiosAuth` to auto-attach JWT.

**Exit Criteria**
- Authenticated calls work without per-request token boilerplate.

### Day 17 — Calculator UI (Interactive Graphs)
**Objective**
- Make calculator results visually dynamic and real-time.

**Build Tasks**
- Build `LoanAmortizationChart.tsx` with Recharts.
- Connect slider state -> API call -> chart update cycle.

**Exit Criteria**
- User interactions trigger backend recalculation and chart rerender.

### Day 18 — Pre-Payment Tool UI
**Objective**
- Expose strategy comparison UX for loan optimization.

**Build Tasks**
- Implement tabs: Strategy 1 / Strategy 2 / Hybrid.
- Add savings bars comparing normal vs optimized interest.
- Add hardcoded “Indicative Only” disclaimer in footer.

**Exit Criteria**
- User can switch strategies and view savings impact immediately.

### Day 19 — Eligibility Wizard (Multi-Step)
**Objective**
- Build resilient guided flow for eligibility capture.

**Build Tasks**
- Use `react-hook-form` + `zod`.
- Steps:
  1. Income + Employment
  2. Current EMI obligations
  3. CIBIL self-declared slider
- Persist partial progress in `sessionStorage`.

**Exit Criteria**
- Refreshing page preserves in-progress wizard data.

### Day 20 — Product Grid & Bank Cards
**Objective**
- Surface filtered bank products in a clear card grid.

**Build Tasks**
- Build `BankCard.tsx` props: `bankLogo`, `interestRate`, `processingFee`, `maxTenure`.
- Integrate `GET /api/v1/public/banks/recommendation`.

**Exit Criteria**
- Cards render accurate API-backed values.

### Day 21 — Admin Dashboard (Auth + Layout)
**Objective**
- Prepare secure admin shell and login flow.

**Build Tasks**
- Build `/admin/login` page.
- Create admin sidebar layout.
- Add `withAuth` HOC for route protection.

**Exit Criteria**
- Unauthenticated users are redirected from admin routes.

### Day 22 — Admin Dashboard (Bank Manager)
**Objective**
- Enable admin interest-rate maintenance from dashboard UI.

**Build Tasks**
- Build TanStack Table for banks.
- Edit action opens modal.
- Save action integrates `PUT /api/v1/admin/banks`.

**Exit Criteria**
- Inline admin edits persist and refresh correctly.

### Day 23 — Reviews & Blogs Components
**Objective**
- Complete trust/content sections for landing experience.

**Build Tasks**
- Build `ReviewCarousel.tsx` (auto-scrolling).
- Build `PinnedBlogCard.tsx`.

**Exit Criteria**
- Components render correctly with mocked or live CMS data.

---

## PHASE 4 — INTEGRATION & PRODUCT WIRING (Days 24–30)
**Goal by Day 30:** End-to-end flows behave like a complete product.

### Day 24 — Lead Dashboard Integration
**Objective**
- Wire lead generation to admin visibility.

**Build Tasks**
- Build admin lead table view.
- Add CSV export using `react-csv`.
- Ensure “Apply Now” posts to lead API.

**Exit Criteria**
- Newly submitted leads appear in admin view and export file.

### Day 25 — Landing Page Assembly
**Objective**
- Assemble full homepage narrative and component flow.

**Build Tasks**
- Build `page.tsx` sections: Hero -> Calculator teaser -> Product grid -> Pinned blogs -> Reviews -> Footer.
- Use `next/image` for assets.

**Exit Criteria**
- Landing page renders all planned sections with no broken assets.

### Day 26 — Mobile Responsiveness Audit
**Objective**
- Ensure usable UX on narrow devices.

**Build Tasks**
- Validate on iPhone SE width (320px).
- Increase slider touch target sizes.
- Collapse multi-column grids to single column.

**Exit Criteria**
- Critical flows remain usable at 320px width.

### Day 27 — SEO Implementation
**Objective**
- Improve discoverability and social sharing quality.

**Build Tasks**
- Configure Next.js Metadata API.
- Add title, description, OpenGraph, canonical tags.
- Generate dynamic `sitemap.xml` from blog data.

**Exit Criteria**
- Metadata and sitemap are accessible and valid.

### Day 28 — Performance Tuning
**Objective**
- Hit production-grade front-end performance baseline.

**Build Tasks**
- Run Lighthouse.
- Lazy-load heavy components (charts) using `next/dynamic`.

**Exit Criteria**
- Performance target exceeds 90 (or documented blockers exist).

### Day 29 — Error & Loading States
**Objective**
- Improve resilience and perceived responsiveness.

**Build Tasks**
- Add `loading.tsx` skeletons.
- Add `not-found.tsx` and global `error.tsx`.

**Exit Criteria**
- Route-level loading/errors show controlled UX states.

### Day 30 — End-to-End Walkthrough
**Objective**
- Validate complete user and admin journey.

**Build Tasks**
- Execute flow: Landing -> Calculate -> Eligibility -> Apply -> Admin lead review.
- Fix broken links or blocked transitions immediately.

**Exit Criteria**
- End-to-end happy path completes without manual DB intervention.

---

## PHASE 5 — INFRASTRUCTURE & DEPLOYMENT (Days 31–37)
**Goal by Day 37:** Staging deployment is live and stable.

### Day 31 — Dockerization (Backend)
**Objective**
- Containerize backend with reproducible build/runtime.

**Build Tasks**
- Create multi-stage Dockerfile (Maven build -> slim JRE runtime).
- Validate with `docker run -p 8080:8080 pryme-backend`.

**Exit Criteria**
- Containerized app boots and serves APIs.

### Day 32 — Cloud Database Setup
**Objective**
- Stand up secure managed PostgreSQL.

**Build Tasks**
- Provision AWS RDS or DigitalOcean managed PostgreSQL.
- Restrict DB access to app server IP(s).
- Enable daily backups.

**Exit Criteria**
- App can connect to managed DB using restricted network policy.

### Day 33 — Backend Deployment
**Objective**
- Deploy backend to a managed runtime.

**Build Tasks**
- Deploy container to Render/Railway/EC2.
- Configure env vars: `DB_URL`, `DB_PASSWORD`, `JWT_SECRET`, `CORS_ORIGIN`.
- Verify cloud Swagger endpoint.

**Exit Criteria**
- Cloud backend serves APIs and docs without startup errors.

### Day 34 — Frontend Deployment
**Objective**
- Publish frontend in production-like hosting.

**Build Tasks**
- Deploy on Vercel.
- Set `NEXT_PUBLIC_API_URL`.
- Ensure production build passes.

**Exit Criteria**
- Hosted frontend reads from deployed backend endpoints.

### Day 35 — SSL & Domain Configuration
**Objective**
- Move from staging URLs to branded secure domains.

**Build Tasks**
- Configure DNS:
  - `api.pryme.in` -> backend
  - `www.pryme.in` -> frontend
- Enforce HTTPS.

**Exit Criteria**
- Domain endpoints resolve and redirect securely over TLS.

### Day 36 — Security Audit (OWASP)
**Objective**
- Reduce exploitable risks before launch.

**Build Tasks**
- Verify rate limiting strategy.
- Validate SQLi protection on custom queries.
- Confirm XSS-safe rendering paths.

**Exit Criteria**
- Audit report produced with critical findings resolved or mitigated.

### Day 37 — Staging Environment Live
**Objective**
- Freeze reliable staging baseline for QA/UAT.

**Build Tasks**
- Publish staging (e.g., `staging.pryme.in`).
- Restrict access to internal team.

**Exit Criteria**
- Staging remains stable for internal walkthrough.

---

## PHASE 6 — QA & HANDOVER PREP (Days 38–45)
**Goal by Day 45:** UAT-ready handover.

### Day 38 — Load Testing
**Objective**
- Validate service behavior under expected burst traffic.

**Build Tasks**
- Run JMeter scenario with 50 concurrent calculator users.
- Track p95 latency and throughput.

**Exit Criteria**
- Calculator API remains near target latency (<=200ms) or scaling action is documented.

### Day 39 — Cross-Browser Testing
**Objective**
- Ensure consistent behavior on major browsers/devices.

**Build Tasks**
- Test Chrome, Firefox, Safari, Edge.
- Resolve rendering and interaction inconsistencies.

**Exit Criteria**
- No high-severity browser-specific defects remain.

### Day 40 — Internal Bug Bash
**Objective**
- Stress test product with adversarial and edge inputs.

**Build Tasks**
- Team-wide exploratory testing.
- Try negatives, emojis, oversized numbers, malformed payloads.
- Log findings in Jira/Trello with severity.

**Exit Criteria**
- Bug list triaged with owners and deadlines.

### Day 41 — Bug Fix Sprint
**Objective**
- Burn down high and critical defects.

**Build Tasks**
- Fix prioritized issues from bug bash.
- Re-test fixed paths.

**Exit Criteria**
- Critical issues are closed and regression-tested.

### Day 42 — Content Population
**Objective**
- Replace placeholders with realistic launch content.

**Build Tasks**
- Upload actual bank logos.
- Enter real bank rates (e.g., HDFC 8.75%).
- Publish 3 high-quality blog posts.

**Exit Criteria**
- Public pages reflect production-grade content quality.

### Day 43 — Documentation
**Objective**
- Make admin operations self-serve for business users.

**Build Tasks**
- Produce Admin User Manual (PDF).
- Include step-by-step screenshots for:
  - Changing rates
  - Downloading leads
  - Pinning blogs

**Exit Criteria**
- Manual reviewed and accepted by internal stakeholders.

### Day 44 — Final Production Build
**Objective**
- Execute release cut with final pre-launch checks.

**Build Tasks**
- Merge staging -> main.
- Deploy production build.
- Re-verify “Indicative Only” disclaimers.

**Exit Criteria**
- Production release signed off by engineering + product.

### Day 45 — Project Completion
**Objective**
- Close project execution and hand over for UAT.

**Build Tasks**
- Enforce code freeze.
- Notify client: “Project ready for UAT.”
- Raise invoice for remaining 40% + GST.

**Exit Criteria**
- UAT handover formally acknowledged.

---

## Daily Tracking Template (Copy/Paste)
Use this for every day to maintain execution discipline:

```md
## Day X — <Title>
- Status: Not Started / In Progress / Blocked / Done
- Objective:
- Planned Tasks:
  - [ ]
  - [ ]
- Completed Today:
  -
- Evidence (PRs, test runs, URLs, screenshots):
  -
- Blockers:
  -
- Carryover to Tomorrow:
  -
```
