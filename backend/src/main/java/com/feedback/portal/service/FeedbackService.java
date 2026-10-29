
package com.feedback.portal.service;

import com.feedback.portal.repository.FeedbackSpecifications;
import com.feedback.portal.dto.*;
import com.feedback.portal.entity.*;
import com.feedback.portal.repository.*;
import com.feedback.portal.security.SubmissionRateLimiter;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeedbackService {
    private final SubmissionRateLimiter submissionRateLimiter;
    private final FeedbackRepository feedbackRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepo;
    private final ModerationDecisionRepository decisionRepo;
    private final AuditEventRepository auditRepo;
    private final AbuseFlagRepository abuseFlagRepo;

    public FeedbackService(
        FeedbackRepository feedbackRepo,
        CategoryRepository categoryRepo,
        UserRepository userRepo,
        ModerationDecisionRepository decisionRepo,
        AuditEventRepository auditRepo,
        AbuseFlagRepository abuseFlagRepo,
        SubmissionRateLimiter submissionRateLimiter) {

    this.feedbackRepo = feedbackRepo;
    this.categoryRepo = categoryRepo;
    this.userRepo = userRepo;
    this.decisionRepo = decisionRepo;
    this.auditRepo = auditRepo;
    this.abuseFlagRepo = abuseFlagRepo;
    this.submissionRateLimiter = submissionRateLimiter;
}

    private FeedbackResponse toResponse(Feedback f) {
        return new FeedbackResponse(
            f.getId(),
            f.getCategory().getName(),
            f.getRating(),
            f.getText(),
            f.getStatus(),
            f.isPrivateFeedback(),
            f.getCreatedAt()
        );
    }

    @Transactional
    public FeedbackResponse submit(
            String email, FeedbackRequest request) {

        User customer = userRepo.findByEmailIgnoreCase(email)
    .orElseThrow(() ->
        new IllegalArgumentException("User not found"));

        submissionRateLimiter.checkLimit(customer.getEmail());

        Category category = categoryRepo.findById(request.categoryId())
            .filter(Category::isActive)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid category"));

        Feedback feedback = new Feedback(
            customer,
            category,
            request.rating(),
            request.text(),
            request.privateFeedback()
        );

        feedback = feedbackRepo.save(feedback);

        auditRepo.save(new AuditEvent(
            customer,
            "FEEDBACK_SUBMITTED",
            "Feedback",
            feedback.getId(),
            "Feedback submitted"
        ));

        return toResponse(feedback);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> publicFeedback(
            String search,
            Long categoryId,
            Integer rating,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 50),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return feedbackRepo.findAll(
            FeedbackSpecifications.publicFeedback(
                search, categoryId, rating
            ),
            pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> myFeedback(
            String email, int page, int size) {

        User customer = userRepo.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 50),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return feedbackRepo.findByCustomerId(
            customer.getId(), pageable
        ).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<FeedbackResponse> pendingFeedback(
            int page, int size) {

        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 50),
            Sort.by(Sort.Direction.ASC, "createdAt")
        );

        return feedbackRepo.findByStatus(
            FeedbackStatus.PENDING, pageable
        ).map(this::toResponse);
    }

    @Transactional
    public FeedbackResponse moderate(
            String moderatorEmail,
            Long feedbackId,
            ModerateRequest request) {

        if (request.decision() != FeedbackStatus.APPROVED
                && request.decision() != FeedbackStatus.REJECTED
                && request.decision() != FeedbackStatus.HIDDEN) {

            throw new IllegalArgumentException(
                "Invalid moderation decision");
        }

        User moderator = userRepo.findByEmailIgnoreCase(moderatorEmail)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found"));

        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() ->
                new IllegalArgumentException("Feedback not found"));

        feedback.setStatus(request.decision());
        feedbackRepo.save(feedback);

        decisionRepo.save(new ModerationDecision(
            feedback,
            moderator,
            request.decision(),
            request.reason()
        ));

        auditRepo.save(new AuditEvent(
            moderator,
            "FEEDBACK_MODERATED",
            "Feedback",
            feedbackId,
            request.decision() + ": " + request.reason()
        ));

        return toResponse(feedback);
    }

    // Report feedback that may violate portal guidelines.
    @Transactional
    public void flagFeedback(
            String reporterEmail,
            Long feedbackId,
            AbuseFlagRequest request) {

        String reason = request.reason().trim();

        if (reason.isBlank() || reason.length() > 500) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A valid reason of at most 500 characters is required"
            );
        }

        User reporter = userRepo.findByEmailIgnoreCase(reporterEmail)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
                ));

        Feedback feedback = feedbackRepo.findById(feedbackId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Feedback not found"
                ));

        boolean alreadyFlagged =
            abuseFlagRepo.existsByFeedbackIdAndReporterId(
                feedbackId, reporter.getId()
            );

        if (alreadyFlagged) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "You have already reported this feedback"
            );
        }

        AbuseFlag flag = abuseFlagRepo.save(
            new AbuseFlag(feedback, reporter, reason)
        );

        auditRepo.save(new AuditEvent(
            reporter,
            "FEEDBACK_FLAGGED",
            "AbuseFlag",
            flag.getId(),
            "Feedback ID " + feedbackId
                + " reported: " + reason
        ));
    }

 
@Transactional(readOnly = true)
public DashboardStatsResponse getDashboardStats(String email) {

    User customer = userRepo.findByEmailIgnoreCase(email)
        .orElseThrow(() ->
            new IllegalArgumentException("User not found"));

    long total = feedbackRepo.countByCustomerId(
        customer.getId()
    );

    double average = feedbackRepo.calculateAverageRatingByCustomerId(
        customer.getId()
    );

    long pending = feedbackRepo.countByCustomerIdAndStatus(
        customer.getId(),
        FeedbackStatus.PENDING
    );

    return new DashboardStatsResponse(total, average, pending);
}

@Transactional(readOnly = true)
public Page<AbuseFlagResponse> getUnresolvedAbuseFlags(
        int page, int size) {

    Pageable pageable = PageRequest.of(
        Math.max(page, 0),
        Math.min(Math.max(size, 1), 50),
        Sort.by(Sort.Direction.ASC, "createdAt")
    );

    return abuseFlagRepo.findByResolvedFalse(pageable)
        .map(flag -> new AbuseFlagResponse(
            flag.getId(),
            flag.getFeedback().getId(),
            flag.getFeedback().getCategory().getName(),
            flag.getFeedback().getText(),
            flag.getFeedback().getStatus(),
            flag.getReporter().getEmail(),
            flag.getReason(),
            flag.getCreatedAt(),
            flag.isResolved()
        ));
}

@Transactional
public void resolveAbuseFlag(
        String moderatorEmail, Long flagId) {

    User moderator = userRepo.findByEmailIgnoreCase(moderatorEmail)
        .orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
            ));

    AbuseFlag flag = abuseFlagRepo.findById(flagId)
        .orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Abuse report not found"
            ));

    if (flag.isResolved()) {
        throw new ResponseStatusException(
            HttpStatus.CONFLICT,
            "Abuse report is already resolved"
        );
    }

    flag.setResolved(true);
    abuseFlagRepo.save(flag);

    auditRepo.save(new AuditEvent(
        moderator,
        "ABUSE_FLAG_RESOLVED",
        "AbuseFlag",
        flag.getId(),
        "Abuse report resolved"
    ));
}

}