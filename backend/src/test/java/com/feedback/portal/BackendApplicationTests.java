
package com.feedback.portal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that the Spring application context starts.
    }

    @Test
    void unauthenticatedUserCannotSubmitFeedback() throws Exception {
        mockMvc.perform(post("/api/feedback")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "categoryId": 1,
                        "rating": 5,
                        "text": "Test feedback",
                        "privateFeedback": false
                    }
                    """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotAccessAdminFeedback() throws Exception {
        mockMvc.perform(get("/api/admin/feedback")
                .with(httpBasic("customer@example.com", "CustomerDemo123!")))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRatingIsRejected() throws Exception {
        mockMvc.perform(post("/api/feedback")
                .with(httpBasic("customer@example.com", "CustomerDemo123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "categoryId": 1,
                        "rating": 0,
                        "text": "Test feedback",
                        "privateFeedback": false
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankFeedbackTextIsRejected() throws Exception {
        mockMvc.perform(post("/api/feedback")
                .with(httpBasic("customer@example.com", "CustomerDemo123!"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "categoryId": 1,
                        "rating": 5,
                        "text": "   ",
                        "privateFeedback": false
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publicFeedbackEndpointRequiresNoAuth() throws Exception {
        mockMvc.perform(get("/api/feedback/public"))
               .andExpect(status().isOk());
    }

    @Test
    void categoriesEndpointRequiresNoAuth() throws Exception {
        mockMvc.perform(get("/api/categories"))
               .andExpect(status().isOk());
    }
}
