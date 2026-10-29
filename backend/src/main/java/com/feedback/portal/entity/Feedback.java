
package com.feedback.portal.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;

    @NotBlank
    @Size(max = 3000)
    @Column(nullable = false, length = 3000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.PENDING;

    @Column(nullable = false)
    private boolean privateFeedback = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private Long version;

    public Feedback() {}

    public Feedback(User customer, Category category,
                    Integer rating, String text,
                    boolean privateFeedback) {
        this.customer = customer;
        this.category = category;
        this.rating = rating;
        this.text = text;
        this.privateFeedback = privateFeedback;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public Category getCategory() { return category; }
    public Integer getRating() { return rating; }
    public String getText() { return text; }
    public FeedbackStatus getStatus() { return status; }
    public boolean isPrivateFeedback() { return privateFeedback; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setPrivateFeedback(boolean privateFeedback) {
        this.privateFeedback = privateFeedback;
    }

    public void setStatus(FeedbackStatus status) {
        this.status = status;
    }
}
