
package com.feedback.portal;

import com.feedback.portal.entity.*;
import com.feedback.portal.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityApiTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository userRepo;
    @Autowired CategoryRepository categoryRepo;
    @Autowired FeedbackRepository feedbackRepo;
    @Autowired PasswordEncoder passwordEncoder;

    String customerAuth;
    String moderatorAuth;
    String adminAuth;
    Category category;

    @BeforeEach
    void setUp() {
        User customer = userRepo.save(new User(
            "api.customer@example.com",
            passwordEncoder.encode("TestPassword123!"),
            UserRole.CUSTOMER
        ));
        User moderator = userRepo.save(new User(
            "api.moderator@example.com",
            passwordEncoder.encode("TestPassword123!"),
            UserRole.MODERATOR
        ));
        User admin = userRepo.save(new User(
            "api.admin@example.com",
            passwordEncoder.encode("TestPassword123!"),
            UserRole.ADMINISTRATOR
        ));
        category = categoryRepo.save(new Category("APITestCategory", "For API tests"));

        customerAuth = basicAuth("api.customer@example.com", "TestPassword123!");
        moderatorAuth = basicAuth("api.moderator@example.com", "TestPassword123!");
        adminAuth = basicAuth("api.admin@example.com", "TestPassword123!");
    }

    // ─── Public Endpoint ─────────────────────────────────────────────────────

    @Test
    void publicFeedback_noAuth_returns200() throws Exception {
        mvc.perform(get("/api/feedback/public"))
            .andExpect(status().isOk());
    }

    // ─── Authentication ───────────────────────────────────────────────────────

    @Test
    void submitFeedback_noAuth_returns401() throws Exception {
        mvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\":1,\"rating\":3,\"text\":\"Test\",\"privateFeedback\":false}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void myFeedback_noAuth_returns401() throws Exception {
        mvc.perform(get("/api/feedback/my"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void moderationEndpoint_noAuth_returns401() throws Exception {
        mvc.perform(get("/api/admin/feedback"))
            .andExpect(status().isUnauthorized());
    }

    // ─── Authorization: Role Escalation ──────────────────────────────────────

    @Test
    void customerAccessingModerationEndpoint_returns403() throws Exception {
        mvc.perform(get("/api/admin/feedback")
                .header("Authorization", customerAuth))
            .andExpect(status().isForbidden());
    }

    @Test
    void customerAccessingAdminUsers_returns403() throws Exception {
        mvc.perform(get("/api/admin/users")
                .header("Authorization", customerAuth))
            .andExpect(status().isForbidden());
    }

    @Test
    void moderatorAccessingAdminUsers_returns403() throws Exception {
        mvc.perform(get("/api/admin/users")
                .header("Authorization", moderatorAuth))
            .andExpect(status().isForbidden());
    }

    // ─── Valid Role Access ────────────────────────────────────────────────────

    @Test
    void moderator_canAccessModerationQueue() throws Exception {
        mvc.perform(get("/api/admin/feedback")
                .header("Authorization", moderatorAuth))
            .andExpect(status().isOk());
    }

    @Test
    void admin_canAccessAdminUsers() throws Exception {
        mvc.perform(get("/api/admin/users")
                .header("Authorization", adminAuth))
            .andExpect(status().isOk());
    }

    @Test
    void admin_canAccessAuditLog() throws Exception {
        mvc.perform(get("/api/admin/audit")
                .header("Authorization", adminAuth))
            .andExpect(status().isOk());
    }

    // ─── Feedback Submission ─────────────────────────────────────────────────

    @Test
    void submitFeedback_validCustomer_returns200() throws Exception {
        mvc.perform(post("/api/feedback")
                .header("Authorization", customerAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                    "{\"categoryId\":%d,\"rating\":4," +
                    "\"text\":\"Great product!\",\"privateFeedback\":false}",
                    category.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void submitFeedback_invalidRating_returns400() throws Exception {
        mvc.perform(post("/api/feedback")
                .header("Authorization", customerAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                    "{\"categoryId\":%d,\"rating\":9," +
                    "\"text\":\"Rating too high\",\"privateFeedback\":false}",
                    category.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitFeedback_emptyText_returns400() throws Exception {
        mvc.perform(post("/api/feedback")
                .header("Authorization", customerAuth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format(
                    "{\"categoryId\":%d,\"rating\":3," +
                    "\"text\":\"\",\"privateFeedback\":false}",
                    category.getId())))
            .andExpect(status().isBadRequest());
    }

    // ─── Horizontal Privilege Escalation Prevention ───────────────────────────

    @Test
    void moderatorCannotAccessAnotherCustomerSubmissions_viaMyEndpoint()
            throws Exception {
        // /api/feedback/my is restricted to CUSTOMER role
        mvc.perform(get("/api/feedback/my")
                .header("Authorization", moderatorAuth))
            .andExpect(status().isForbidden());
    }

    // ─── Admin Stats ──────────────────────────────────────────────────────────

    @Test
    void adminStats_returnsCorrectStructure() throws Exception {
        mvc.perform(get("/api/admin/stats")
                .header("Authorization", adminAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUsers").exists())
            .andExpect(jsonPath("$.totalFeedback").exists());
    }

    // ─── Dashboard Stats ──────────────────────────────────────────────────────

    @Test
    void feedbackStats_customer_returnsOk() throws Exception {
        mvc.perform(get("/api/feedback/stats")
                .header("Authorization", customerAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalFeedback").exists());
    }

    // ─── Categories ───────────────────────────────────────────────────────────

    @Test
    void categories_noAuth_returnsOk() throws Exception {
        mvc.perform(get("/api/categories"))
            .andExpect(status().isOk());
    }

    // ─── Abuse Flag ───────────────────────────────────────────────────────────

    @Test
    void flagFeedback_noAuth_returns401() throws Exception {
        mvc.perform(post("/api/feedback/1/flags")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"spam\"}"))
            .andExpect(status().isUnauthorized());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String basicAuth(String username, String password) {
        String credentials = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
            credentials.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
