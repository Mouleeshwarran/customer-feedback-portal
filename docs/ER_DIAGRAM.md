# Customer Feedback Portal (FeedbackHub) — Entity Relationship Diagram

## 1. Entity Relationship (Mermaid)

```mermaid
erDiagram
    USER ||--o{ FEEDBACK : "submits"
    USER ||--o{ MODERATION_DECISION : "records"
    USER ||--o{ ABUSE_FLAG : "reports"
    USER ||--o{ AUDIT_EVENT : "triggers"
    CATEGORY ||--o{ FEEDBACK : "classifies"
    FEEDBACK ||--o{ ATTACHMENT : "contains"
    FEEDBACK ||--o{ MODERATION_DECISION : "receives"
    FEEDBACK ||--o{ ABUSE_FLAG : "targeted_by"

    USER {
        bigint id PK
        string email UK "NOT NULL, max 150"
        string password_hash "NOT NULL, BCrypt"
        string role "NOT NULL, CUSTOMER|MODERATOR|ADMINISTRATOR"
        boolean active "NOT NULL, default true"
        timestamp created_at "NOT NULL"
        bigint version "Optimistic Locking"
    }

    CATEGORY {
        bigint id PK
        string name UK "NOT NULL, max 60"
        string description "max 300"
        boolean active "NOT NULL, default true"
    }

    FEEDBACK {
        bigint id PK
        bigint customer_id FK "NOT NULL -> USER(id)"
        bigint category_id FK "NOT NULL -> CATEGORY(id)"
        int rating "NOT NULL, CHECK (1..5)"
        string text "NOT NULL, max 3000"
        string status "NOT NULL, PENDING|APPROVED|REJECTED|HIDDEN"
        boolean private_feedback "NOT NULL, default false"
        timestamp created_at "NOT NULL"
        timestamp updated_at "NOT NULL"
        bigint version "Optimistic Locking"
    }

    ATTACHMENT {
        bigint id PK
        bigint feedback_id FK "NOT NULL -> FEEDBACK(id)"
        string original_filename "NOT NULL, max 255"
        string content_type "NOT NULL, max 100"
        string storage_key UK "NOT NULL, max 500 (UUID.ext)"
        bigint file_size "NOT NULL (bytes)"
        timestamp created_at "NOT NULL"
    }

    MODERATION_DECISION {
        bigint id PK
        bigint feedback_id FK "NOT NULL -> FEEDBACK(id)"
        bigint moderator_id FK "NOT NULL -> USER(id)"
        string decision "NOT NULL, APPROVED|REJECTED|HIDDEN"
        string reason "NOT NULL, max 500"
        timestamp created_at "NOT NULL"
    }

    ABUSE_FLAG {
        bigint id PK
        bigint feedback_id FK "NOT NULL -> FEEDBACK(id)"
        bigint reporter_id FK "NOT NULL -> USER(id)"
        string reason "NOT NULL, max 500"
        boolean resolved "NOT NULL, default false"
        timestamp created_at "NOT NULL"
    }

    AUDIT_EVENT {
        bigint id PK
        bigint actor_id FK "NULLABLE -> USER(id)"
        string action "NOT NULL, max 100"
        string entity_type "max 100"
        bigint entity_id
        string details "max 1000"
        timestamp created_at "NOT NULL"
    }
```

---

## 2. Integrity Constraints & Cardinalities
1. **Users & Feedback**: One-to-Many. When a user is removed, cascaded deletion cleans up associated feedback (or marks inactive via soft-delete in admin flows).
2. **Abuse Prevention**: `ABUSE_FLAG` has a unique constraint `UNIQUE(feedback_id, reporter_id)` to ensure a user cannot submit duplicate flags for the same feedback item.
3. **Attachments**: One-to-Many from Feedback to Attachments. Enforces strict maximum file size (10 MB), allowed MIME types, and UUID storage keys outside the web root.
4. **Optimistic Locking**: Handled on `User` and `Feedback` using JPA `@Version` to prevent concurrent overwrite anomalies during simultaneous updates.
