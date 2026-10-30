
package com.feedback.portal.repository;

import com.feedback.portal.entity.AbuseFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbuseFlagRepository extends JpaRepository<AbuseFlag, Long> {

    boolean existsByFeedbackIdAndReporterId(Long feedbackId, Long reporterId);

    Page<AbuseFlag> findByResolvedFalse(Pageable pageable);

    long countByResolvedFalse();
}
