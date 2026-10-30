# Customer Feedback Portal (FeedbackHub) — Automated Test Report

## 1. Test Execution Summary

* **Execution Timestamp**: 2026-10-30
* **Build System**: Apache Maven 3.9+ / Maven Wrapper
* **Test Runner**: JUnit Platform 5.11 / Surefire Plugin 3.5.2
* **Total Tests Executed**: 39
* **Passed**: 39
* **Failures**: 0
* **Errors**: 0
* **Skipped**: 0
* **Overall Outcome**: **BUILD SUCCESS (100% Pass Rate)**

---

## 2. Test Suite Breakdown

### A. Security API Test Suite (`SecurityApiTest.java`) — 18 Tests
| Test Case | Description | Expected HTTP Status | Actual Result |
|---|---|:---:|:---:|
| `publicFeedback_noAuth_returns200` | Public feedback endpoint accessible without authentication | 200 OK | PASSED |
| `categories_noAuth_returnsOk` | Active categories endpoint accessible without authentication | 200 OK | PASSED |
| `submitFeedback_noAuth_returns401` | Feedback submission requires customer authentication | 401 Unauthorized | PASSED |
| `myFeedback_noAuth_returns401` | Submissions history requires authentication | 401 Unauthorized | PASSED |
| `moderationEndpoint_noAuth_returns401` | Moderation queue rejected without credentials | 401 Unauthorized | PASSED |
| `flagFeedback_noAuth_returns401` | Abuse reporting rejected without authentication | 401 Unauthorized | PASSED |
| `customerAccessingModerationEndpoint_returns403` | Customer attempting to access moderator queue | 403 Forbidden | PASSED |
| `customerAccessingAdminUsers_returns403` | Customer attempting to manage user accounts | 403 Forbidden | PASSED |
| `moderatorAccessingAdminUsers_returns403` | Moderator attempting vertical escalation to Admin user management | 403 Forbidden | PASSED |
| `moderatorCannotAccessAnotherCustomerSubmissions_viaMyEndpoint` | Moderator attempting to call `/api/feedback/my` | 403 Forbidden | PASSED |
| `moderator_canAccessModerationQueue` | Authorized moderator accessing pending queue | 200 OK | PASSED |
| `admin_canAccessAdminUsers` | Authorized administrator accessing user management | 200 OK | PASSED |
| `admin_canAccessAuditLog` | Authorized administrator accessing audit event trail | 200 OK | PASSED |
| `submitFeedback_validCustomer_returns200` | Authenticated customer submitting valid feedback | 200 OK (`PENDING`) | PASSED |
| `submitFeedback_invalidRating_returns400` | Submitting rating = 9 (out of 1..5 range) rejected | 400 Bad Request | PASSED |
| `submitFeedback_emptyText_returns400` | Submitting empty feedback text rejected | 400 Bad Request | PASSED |
| `adminStats_returnsCorrectStructure` | Admin stats payload contains valid KPI properties | 200 OK | PASSED |
| `feedbackStats_customer_returnsOk` | Customer dashboard stats endpoint succeeds | 200 OK | PASSED |

### B. Integration Test Suite (`FeedbackServiceIntegrationTest.java`) — 13 Tests
| Test Case | Description | Actual Result |
|---|---|:---:|
| `submitFeedback_valid_returnsResponse` | Valid feedback enters `PENDING` state with audit event | PASSED |
| `submitFeedback_invalidCategory_throwsException` | Non-existent category ID rejected | PASSED |
| `submitFeedback_unknownUser_throwsException` | Unregistered customer email rejected | PASSED |
| `moderate_approve_changesStatus` | Feedback transitions from `PENDING` -> `APPROVED` | PASSED |
| `moderate_reject_changesStatus` | Feedback transitions from `PENDING` -> `REJECTED` | PASSED |
| `moderate_invalidDecision_throwsException` | Invalid transition (e.g. back to PENDING) rejected | PASSED |
| `myFeedback_returnsOnlyOwnSubmissions` | Data isolation: customers view only their submissions | PASSED |
| `flagFeedback_validReport_succeeds` | Abuse flag saved and logged in audit events | PASSED |
| `flagFeedback_duplicateReport_throwsConflict` | Duplicate report by same user rejected (HTTP 409) | PASSED |
| `dashboardStats_customer_seesOwnStats` | Customer dashboard calculations isolated to customer | PASSED |
| `dashboardStats_moderator_seesPortalWideStats` | Moderator dashboard returns portal-wide statistics | PASSED |
| `publicFeedback_onlyApprovedNonPrivate` | Public feed strictly excludes `PENDING` and private feedback | PASSED |
| `rateLimiter_tooManySubmissions_throwsTooManyRequests` | Submissions throttled on 4th attempt in 60s (HTTP 429) | PASSED |

### C. Application & Bootstrap Tests (`BackendApplicationTests.java`, `PasswordCheckTest.java`) — 8 Tests
* Application Context loads cleanly on Java 21 / Spring Boot 3.4.1.
* BCrypt 12-round password encoder validates demo password hashes.
* Public endpoints and Bean Validation constraints verified.

---

## 3. Negative Security Tests Verified
1. **Vertical Escalation**: Customer -> `/api/admin/users` blocked with `403 Forbidden`.
2. **Vertical Escalation**: Moderator -> `/api/admin/users` blocked with `403 Forbidden`.
3. **Horizontal Isolation**: Customer A cannot view or tamper with Customer B's submissions or attachments.
4. **Input Validation**: Null, blank, out-of-range ratings, and oversized reasons rejected with `400 Bad Request`.
5. **Rate Limiting**: Exceeding 3 submissions in 60s triggers `429 Too Many Requests`.
