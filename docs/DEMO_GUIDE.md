# Customer Feedback Portal (FeedbackHub) — 5 to 10 Minute Demonstration Guide

This demonstration script covers the complete evaluation workflow for academic submission.

---

## Preparation & Starting Services

### 1. Start the Backend
Open a terminal in `D:\ioc\customer-feedback-portal\backend`:
```powershell
./mvnw spring-boot:run
```
*Backend runs at `http://localhost:8080` (H2 Console at `/h2-console`, Swagger UI at `/swagger-ui.html`).*

### 2. Start the Frontend
Open a second terminal in `D:\ioc\customer-feedback-portal\frontend`:
```powershell
npm run dev
```
*Frontend runs at `http://localhost:5173`.*

---

## Demonstration Script

### Workflow 1: Customer Journey (2-3 Minutes)
1. **Login**:
   - Navigate to `http://localhost:5173`.
   - Click the **Customer** demo chip on the login card (`customer@example.com` / `CustomerDemo123!`).
   - Click **Sign in to FeedbackHub**.
2. **Dashboard Review**:
   - Note the customer KPI cards: Total Submissions, Average Rating, Awaiting Review.
3. **Submit Feedback with Attachment**:
   - Click **Submit Feedback**.
   - Select a category (e.g. `Product` or `Service`).
   - Select rating (e.g. `5 Stars`).
   - Type feedback comments: *"The user interface is exceptionally responsive and modern."*
   - Attach an evidence file (e.g., screenshot `.png` or document `.pdf`).
   - Leave "Keep this submission private" unchecked.
   - Click **Submit Feedback**. Notice the success banner: *"Feedback submitted successfully! It is now pending moderation."*
4. **Inspect Personal Submissions**:
   - Click **My Submissions** in the sidebar.
   - Verify the newly submitted item is listed with the status badge `PENDING REVIEW`.
5. **Report Abuse**:
   - Click **Browse Feedback**.
   - On any approved public feedback card, click **Report Abuse**.
   - Type reason: *"Contains inappropriate promotional text"* and submit.
   - Notice the duplicate report check: attempting to report the same feedback again will return a friendly error.

---

### Workflow 2: Moderator Journey (2 Minutes)
1. **Switch to Moderator**:
   - Click **Logout** at the bottom of the sidebar.
   - Click the **Moderator** demo chip (`moderator@example.com` / `ModeratorDemo123!`).
   - Click **Sign in to FeedbackHub**.
2. **Moderator Dashboard**:
   - Notice the moderator portal-wide stats: Total Feedback, Average Rating, Pending Moderation, and Unresolved Abuse Reports badge.
3. **Moderation Queue**:
   - Click **Moderation** in the sidebar.
   - The pending submission from Workflow 1 is visible.
   - In the "Moderation Reason" field, type: *"Verified authentic feedback conforming to standards."*
   - Click **Approve**.
   - The item immediately leaves the queue, and dashboard pending count decreases.
4. **Resolve Abuse Report**:
   - Click **Abuse Reports** in the sidebar.
   - View the report submitted in Workflow 1.
   - Review details and click **Mark as Resolved**.

---

### Workflow 3: Administrator Journey (2 Minutes)
1. **Switch to Administrator**:
   - Click **Logout**.
   - Click the **Administrator** demo chip (`admin@example.com` / `AdminDemo123!`).
   - Click **Sign in to FeedbackHub**.
2. **System-Level KPI Overview**:
   - View the admin KPI cards: Total Users, Portal Feedback, Global Satisfaction, Pending Moderation, Open Reports, and Total Categories.
3. **User Account Management**:
   - Click **Users** in the sidebar.
   - View all registered accounts (`customer@example.com`, `moderator@example.com`, `admin@example.com`).
   - Click **Deactivate** / **Activate** to demonstrate live account state toggling.
4. **Category Management**:
   - Click **Categories** in the sidebar.
   - View configured taxonomies.
   - Click **New Category**, enter *"Billing & Payments"*, description, and save.
   - Notice it immediately becomes available to customers submitting feedback!
5. **Security Audit Trail**:
   - Click **Audit Trail** in the sidebar.
   - Inspect immutable log entries recording customer submission, moderation decisions, report resolutions, and admin changes with exact timestamps.

---

### Workflow 4: Security Demonstration (1 Minute)
1. **API Direct Access & RBAC**:
   - Direct unauthenticated call: `curl -i http://localhost:8080/api/admin/users` -> returns `401 Unauthorized`.
   - Customer calling admin endpoint: `curl -u customer@example.com:CustomerDemo123! http://localhost:8080/api/admin/users` -> returns `403 Forbidden`.
2. **Rate Limiting**:
   - Rapidly submitting feedback 4 times in under 60 seconds triggers `429 Too Many Requests`.
3. **OpenAPI / Swagger UI**:
   - Open `http://localhost:8080/swagger-ui.html` to demonstrate interactive documentation.
