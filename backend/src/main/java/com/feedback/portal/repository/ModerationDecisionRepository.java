
package com.feedback.portal.repository;

import com.feedback.portal.entity.ModerationDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModerationDecisionRepository
        extends JpaRepository<ModerationDecision, Long> {
    List<ModerationDecision> findByFeedbackIdOrderByCreatedAtDesc(Long feedbackId);
}
