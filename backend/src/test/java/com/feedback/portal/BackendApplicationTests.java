
package com.feedback.portal;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies that the Spring application starts.
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
                .with(org.springframework.security.test.web.servlet.request
                    .SecurityMockMvcRequestPostProcessors.httpBasic(
                        "customer@example.com",
                        "CustomerDemo123!"
                    )))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRatingIsRejected() throws Exception {
        mockMvc.perform(post("/api/feedback")
                .with(org.springframework.security.test.web.servlet.request
                    .SecurityMockMvcRequestPostProcessors.httpBasic(
                        "customer@example.com",
                        "CustomerDemo123!"
                    ))
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
                .with(org.springframework.security.test.web.servlet.request
                    .SecurityMockMvcRequestPostProcessors.httpBasic(
                        "customer@example.com",
                        "CustomerDemo123!"
                    ))
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
}
