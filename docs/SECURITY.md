# Customer Feedback Portal (FeedbackHub) — Security & Threat Model

## 1. Threat Model & Risk Analysis

| Threat | Risk Level | Target Component | Mitigating Control |
|---|---|---|---|
| **SQL Injection (SQLi)** | Critical | Search, Filtering, DB Queries | Spring Data JPA Parameterized queries & Criteria API Specifications. Whitelisted sorting fields. |
| **Cross-Site Scripting (Stored XSS)** | High | Feedback Comments & Reasons | React automatic JSX escaping; text treated strictly as strings; no `dangerouslySetInnerHTML`. |
| **Broken Object-Level Auth (BOLA / IDOR)** | High | My Submissions, Attachments | Ownership check (`feedback.getCustomer().getId().equals(user.getId())`) enforced in service layer before returning data. |
| **Vertical Privilege Escalation** | High | Moderation & Admin Endpoints | Spring Security `@EnableMethodSecurity` and URL role matcher (`hasRole('ADMINISTRATOR')` vs `hasAnyRole('MODERATOR', 'ADMINISTRATOR')`). |
| **Unrestricted File Upload** | Critical | Evidence Attachment Upload | Strict MIME whitelist, extension whitelist, 10MB size limit, UUID server-side file renaming, storage outside web root, non-executable storage permissions. |
| **Path Traversal Attack** | High | File Download / Upload | `Paths.get(storageDir).normalize()`, verification with `.startsWith(dir)`, filename sanitization (`replaceAll("[^a-zA-Z0-9._\\-]", "_")`). |
| **Credential Brute-Force** | Medium | Login Endpoint | `LoginRateLimiter`: Sliding window blocks IP/identifier after 5 consecutive failed attempts for 15 minutes. |
| **Spam / Flood Submissions** | Medium | Feedback Submission | `SubmissionRateLimiter`: Max 3 submissions per 60 seconds per customer account. Returns HTTP 429. |
| **Information Leakage** | Low | Error Handling | `GlobalExceptionHandler`: Masks database stack traces; returns generic user-facing messages and a unique `correlationId`. |

---

## 2. Password & Authentication Security
* **Password Policy**: Minimum 12 characters for all locally managed user accounts (enforced in `AdminService` and Bean Validation `@Size(min = 12)`).
* **Password Hashing**: Adaptive BCrypt hashing with work factor of 12 (`BCryptPasswordEncoder(12)`).
* **Zero Plain-Text Storage**: Passwords are never stored or logged in plain text.
* **HTTP Security Headers**:
  * `X-Frame-Options: SAMEORIGIN` (Clickjacking prevention).
  * `X-Content-Type-Options: nosniff` (MIME sniffing prevention).
  * `Referrer-Policy: strict-origin-when-cross-origin`.
  * `Strict-Transport-Security: max-age=31536000; includeSubDomains`.

---

## 3. Role Authorization Matrix

| Endpoint | HTTP Method | Customer | Moderator | Administrator | Unauthenticated |
|---|---|:---:|:---:|:---:|:---:|
| `/api/feedback/public` | GET | Allowed | Allowed | Allowed | Allowed |
| `/api/categories` | GET | Allowed | Allowed | Allowed | Allowed |
| `/api/feedback` | POST | Allowed | Denied (403) | Denied (403) | Denied (401) |
| `/api/feedback/my` | GET | Allowed | Denied (403) | Denied (403) | Denied (401) |
| `/api/feedback/{id}/flags` | POST | Allowed | Allowed | Allowed | Denied (401) |
| `/api/feedback/{id}/attachments` | POST | Own feedback | Denied (403) | Denied (403) | Denied (401) |
| `/api/feedback/{id}/attachments/{aid}/download`| GET | Own feedback | Allowed | Allowed | Denied (401) |
| `/api/admin/feedback` | GET | Denied (403) | Allowed | Allowed | Denied (401) |
| `/api/admin/feedback/{id}/moderate` | PATCH | Denied (403) | Allowed | Allowed | Denied (401) |
| `/api/admin/reports` | GET | Denied (403) | Allowed | Allowed | Denied (401) |
| `/api/admin/reports/{id}/resolve` | PATCH | Denied (403) | Allowed | Allowed | Denied (401) |
| `/api/admin/users/**` | ALL | Denied (403) | Denied (403) | Allowed | Denied (401) |
| `/api/admin/categories/**` | ALL | Denied (403) | Allowed | Allowed | Denied (401) |
| `/api/admin/audit` | GET | Denied (403) | Allowed | Allowed | Denied (401) |
