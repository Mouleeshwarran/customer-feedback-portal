
package com.feedback.portal.repository;

import com.feedback.portal.entity.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByFeedbackId(Long feedbackId);
}
