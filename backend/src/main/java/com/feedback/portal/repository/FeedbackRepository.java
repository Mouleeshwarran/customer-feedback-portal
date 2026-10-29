
package com.feedback.portal.repository;

import com.feedback.portal.entity.Feedback;
import com.feedback.portal.entity.FeedbackStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackRepository extends
        JpaRepository<Feedback, Long>,
        JpaSpecificationExecutor<Feedback> {

    Page<Feedback> findByStatusAndPrivateFeedbackFalse(
        FeedbackStatus status, Pageable pageable);

    Page<Feedback> findByCustomerId(
        Long customerId, Pageable pageable);

    Page<Feedback> findByStatus(
        FeedbackStatus status, Pageable pageable);

    List<Feedback> findByCustomerIdAndStatus(
        Long customerId, FeedbackStatus status);

    long countByStatus(FeedbackStatus status);

    // Global average rating
    @Query("SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f")
    Double calculateAverageRating();

    // Customer-specific total feedback count
    long countByCustomerId(Long customerId);

    // Customer-specific pending feedback count
    long countByCustomerIdAndStatus(
        Long customerId, FeedbackStatus status);

    // Customer-specific average rating
    @Query("""
        SELECT COALESCE(AVG(f.rating), 0.0)
        FROM Feedback f
        WHERE f.customer.id = :customerId
        """)
    Double calculateAverageRatingByCustomerId(
        @Param("customerId") Long customerId);
}
