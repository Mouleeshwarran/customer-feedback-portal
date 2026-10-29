
package com.feedback.portal.repository;

import com.feedback.portal.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByFeedbackId(Long feedbackId);
}
