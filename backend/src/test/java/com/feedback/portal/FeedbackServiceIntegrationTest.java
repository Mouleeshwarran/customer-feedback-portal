
package com.feedback.portal;

import com.feedback.portal.dto.*;
import com.feedback.portal.entity.*;
import com.feedback.portal.repository.*;
import com.feedback.portal.service.FeedbackService;
import com.feedback.portal.security.SubmissionRateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FeedbackServiceIntegrationTest {

    @Autowired FeedbackService feedbackService;
    @Autowired UserRepository userRepo;
    @Autowired CategoryRepository categoryRepo;
    @Autowired FeedbackRepository feedbackRepo;
    @Autowired AbuseFlagRepository abuseFlagRepo;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired SubmissionRateLimiter submissionRateLimiter;

    User customer;
    User moderator;
    Category category;

    @BeforeEach
    void setUp() {
        submissionRateLimiter.reset();
        customer = userRepo.save(new User(
            "test.customer@example.com",
            passwordEncoder.encode("TestPassword123!"),
            UserRole.CUSTOMER
        ));
        moderator = userRepo.save(new User(
            "test.moderator@example.com",
            passwordEncoder.encode("TestPassword123!"),
            UserRole.MODERATOR
        ));
        category = categoryRepo.save(new Category("TestCategory", "For testing"));
    }

    // ─── Feedback Submission ─────────────────────────────────────────────────

    @Test
    void submitFeedback_valid_returnsResponse() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 4, "Great product!", false);
        FeedbackResponse resp = feedbackService.submit(customer.getEmail(), req);

        assertThat(resp).isNotNull();
        assertThat(resp.status()).isEqualTo(FeedbackStatus.PENDING);
        assertThat(resp.rating()).isEqualTo(4);
        assertThat(resp.text()).isEqualTo("Great product!");
    }

    @Test
    void submitFeedback_invalidCategory_throwsException() {
        FeedbackRequest req = new FeedbackRequest(99999L, 3, "Test", false);
        assertThatThrownBy(() -> feedbackService.submit(customer.getEmail(), req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid category");
    }

    @Test
    void submitFeedback_unknownUser_throwsException() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 3, "Test", false);
        assertThatThrownBy(() -> feedbackService.submit("nobody@example.com", req))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── Moderation ──────────────────────────────────────────────────────────

    @Test
    void moderate_approve_changesStatus() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 5, "Excellent!", false);
        FeedbackResponse submitted = feedbackService.submit(customer.getEmail(), req);

        ModerateRequest modReq = new ModerateRequest(
            FeedbackStatus.APPROVED, "Looks good");
        FeedbackResponse moderated = feedbackService.moderate(
            moderator.getEmail(), submitted.id(), modReq);

        assertThat(moderated.status()).isEqualTo(FeedbackStatus.APPROVED);
    }

    @Test
    void moderate_reject_changesStatus() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 1, "Bad service", false);
        FeedbackResponse submitted = feedbackService.submit(customer.getEmail(), req);

        ModerateRequest modReq = new ModerateRequest(
            FeedbackStatus.REJECTED, "Violates guidelines");
        FeedbackResponse moderated = feedbackService.moderate(
            moderator.getEmail(), submitted.id(), modReq);

        assertThat(moderated.status()).isEqualTo(FeedbackStatus.REJECTED);
    }

    @Test
    void moderate_invalidDecision_throwsException() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 3, "Neutral", false);
        FeedbackResponse submitted = feedbackService.submit(customer.getEmail(), req);

        // Trying to moderate to PENDING is not allowed
        ModerateRequest modReq = new ModerateRequest(
            FeedbackStatus.PENDING, "Reset");
        assertThatThrownBy(() ->
            feedbackService.moderate(moderator.getEmail(), submitted.id(), modReq))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── Customer Ownership ──────────────────────────────────────────────────

    @Test
    void myFeedback_returnsOnlyOwnSubmissions() {
        FeedbackRequest req1 = new FeedbackRequest(
            category.getId(), 4, "Mine", false);
        feedbackService.submit(customer.getEmail(), req1);

        var page = feedbackService.myFeedback(customer.getEmail(), 0, 10);
        assertThat(page.getContent()).allMatch(
            f -> f.text().equals("Mine"));
    }

    // ─── Abuse Flags ─────────────────────────────────────────────────────────

    @Test
    void flagFeedback_duplicateReport_throwsConflict() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 2, "Problematic content", false);
        FeedbackResponse submitted = feedbackService.submit(customer.getEmail(), req);

        User reporter = userRepo.save(new User(
            "reporter@example.com",
            passwordEncoder.encode("ReporterPass123!"),
            UserRole.CUSTOMER
        ));

        AbuseFlagRequest flagReq = new AbuseFlagRequest("Spam content");
        feedbackService.flagFeedback(reporter.getEmail(), submitted.id(), flagReq);

        // Second report from same user should fail
        assertThatThrownBy(() ->
            feedbackService.flagFeedback(reporter.getEmail(), submitted.id(), flagReq))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
    }

    @Test
    void flagFeedback_validReport_succeeds() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 3, "Possibly problematic", false);
        FeedbackResponse submitted = feedbackService.submit(customer.getEmail(), req);

        User reporter = userRepo.save(new User(
            "reporter2@example.com",
            passwordEncoder.encode("ReporterPass123!"),
            UserRole.CUSTOMER
        ));

        AbuseFlagRequest flagReq = new AbuseFlagRequest("Offensive language");
        assertThatCode(() ->
            feedbackService.flagFeedback(reporter.getEmail(), submitted.id(), flagReq))
            .doesNotThrowAnyException();
    }

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    @Test
    void dashboardStats_customer_seesOwnStats() {
        FeedbackRequest req = new FeedbackRequest(
            category.getId(), 5, "Wonderful!", false);
        feedbackService.submit(customer.getEmail(), req);

        DashboardStatsResponse stats = feedbackService.getDashboardStats(customer.getEmail());
        assertThat(stats.totalFeedback()).isGreaterThanOrEqualTo(1L);
        assertThat(stats.averageRating()).isGreaterThan(0.0);
        assertThat(stats.pendingFeedback()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void dashboardStats_moderator_seesPortalWideStats() {
        DashboardStatsResponse stats = feedbackService.getDashboardStats(moderator.getEmail());
        assertThat(stats).isNotNull();
    }

    // ─── Public Feedback ─────────────────────────────────────────────────────

    @Test
    void publicFeedback_onlyApprovedNonPrivate() {
        // Submit and approve one
        FeedbackRequest req1 = new FeedbackRequest(
            category.getId(), 5, "Public approved", false);
        FeedbackResponse submitted1 = feedbackService.submit(customer.getEmail(), req1);
        feedbackService.moderate(moderator.getEmail(), submitted1.id(),
            new ModerateRequest(FeedbackStatus.APPROVED, "OK"));

        // Submit a private one and approve it — should NOT appear
        FeedbackRequest req2 = new FeedbackRequest(
            category.getId(), 4, "Private approved", true);
        FeedbackResponse submitted2 = feedbackService.submit(customer.getEmail(), req2);
        feedbackService.moderate(moderator.getEmail(), submitted2.id(),
            new ModerateRequest(FeedbackStatus.APPROVED, "OK"));

        // Submit and keep pending — should NOT appear
        FeedbackRequest req3 = new FeedbackRequest(
            category.getId(), 3, "Pending feedback", false);
        feedbackService.submit(customer.getEmail(), req3);

        var publicPage = feedbackService.publicFeedback("", null, null, 0, 50);
        assertThat(publicPage.getContent())
            .allMatch(f -> !f.privateFeedback())
            .allMatch(f -> f.status() == FeedbackStatus.APPROVED);
    }

    // ─── Rate Limiting ────────────────────────────────────────────────────────

    @Test
    void rateLimiter_tooManySubmissions_throwsTooManyRequests() {
        SubmissionRateLimiter limiter = new SubmissionRateLimiter();
        String email = "ratelimitTest@example.com";

        limiter.checkLimit(email);
        limiter.checkLimit(email);
        limiter.checkLimit(email);

        assertThatThrownBy(() -> limiter.checkLimit(email))
            .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
            .extracting("statusCode")
            .isEqualTo(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS);
    }
}
