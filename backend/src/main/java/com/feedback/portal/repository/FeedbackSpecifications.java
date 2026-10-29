
package com.feedback.portal.repository;

import com.feedback.portal.entity.Feedback;
import com.feedback.portal.entity.FeedbackStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class FeedbackSpecifications {

    private FeedbackSpecifications() {}

    public static Specification<Feedback> publicFeedback(
            String search, Long categoryId, Integer rating) {

        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Public feedback must be approved and non-private.
            predicates.add(
                criteriaBuilder.equal(
                    root.get("status"), FeedbackStatus.APPROVED
                )
            );

            predicates.add(
                criteriaBuilder.isFalse(
                    root.get("privateFeedback")
                )
            );

            // Search within the feedback text, ignoring case.
            if (search != null && !search.isBlank()) {
                String pattern = "%"
                    + search.trim().toLowerCase()
                    + "%";

                predicates.add(
                    criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("text")),
                        pattern
                    )
                );
            }

            // Filter by category ID when selected.
            if (categoryId != null) {
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("category").get("id"),
                        categoryId
                    )
                );
            }

            // Filter by rating when selected.
            if (rating != null) {
                predicates.add(
                    criteriaBuilder.equal(
                        root.get("rating"), rating
                    )
                );
            }

            return criteriaBuilder.and(
                predicates.toArray(new Predicate[0])
            );
        };
    }
}
