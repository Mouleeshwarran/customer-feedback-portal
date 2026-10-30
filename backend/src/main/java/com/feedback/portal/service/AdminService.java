
package com.feedback.portal.service;

import com.feedback.portal.dto.*;
import com.feedback.portal.entity.*;
import com.feedback.portal.repository.*;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminService {

    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final AuditEventRepository auditRepo;
    private final AbuseFlagRepository abuseFlagRepo;
    private final FeedbackRepository feedbackRepo;
    private final PasswordEncoder passwordEncoder;

    public AdminService(
            UserRepository userRepo,
            CategoryRepository categoryRepo,
            AuditEventRepository auditRepo,
            AbuseFlagRepository abuseFlagRepo,
            FeedbackRepository feedbackRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
        this.auditRepo = auditRepo;
        this.abuseFlagRepo = abuseFlagRepo;
        this.feedbackRepo = feedbackRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // ─── User Management ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<UserResponse> listUsers(int page, int size) {
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 100),
            Sort.by("createdAt").descending()
        );
        return userRepo.findAll(pageable).map(this::toUserResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        return toUserResponse(findUser(id));
    }

    @Transactional
    public UserResponse createUser(String adminEmail, UserRequest request) {
        if (userRepo.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "A user with this email already exists");
        }
        if (request.password() == null || request.password().length() < 12) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Password must be at least 12 characters");
        }
        User user = new User(
            request.email().trim().toLowerCase(),
            passwordEncoder.encode(request.password()),
            request.role()
        );
        user.setActive(request.active());
        user = userRepo.save(user);

        audit(adminEmail, "USER_CREATED", "User", user.getId(),
            "Created user: " + user.getEmail() + " role=" + user.getRole());

        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(String adminEmail, Long id, UserRequest request) {
        User user = findUser(id);

        // Prevent self-role-downgrade (soft protection)
        user.setRole(request.role());
        user.setActive(request.active());

        if (request.password() != null && !request.password().isBlank()) {
            if (request.password().length() < 12) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Password must be at least 12 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        user = userRepo.save(user);

        audit(adminEmail, "USER_UPDATED", "User", user.getId(),
            "Updated user: " + user.getEmail());

        return toUserResponse(user);
    }

    @Transactional
    public void setUserActive(String adminEmail, Long id, boolean active) {
        User user = findUser(id);
        user.setActive(active);
        userRepo.save(user);
        audit(adminEmail, active ? "USER_ACTIVATED" : "USER_DEACTIVATED",
            "User", id, "User: " + user.getEmail());
    }

    // ─── Category Management ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<CategoryResponse> listCategories(int page, int size) {
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 100)
        );
        return categoryRepo.findAll(pageable).map(this::toCategoryResponse);
    }

    @Transactional(readOnly = true)
    public java.util.List<CategoryResponse> listActiveCategories() {
        return categoryRepo.findByActiveTrue()
            .stream()
            .map(this::toCategoryResponse)
            .toList();
    }

    @Transactional
    public CategoryResponse createCategory(String adminEmail, CategoryRequest req) {
        if (categoryRepo.findByNameIgnoreCase(req.name()).isPresent()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "A category with this name already exists");
        }
        Category cat = new Category(req.name().trim(), req.description());
        cat.setActive(req.active());
        cat = categoryRepo.save(cat);
        audit(adminEmail, "CATEGORY_CREATED", "Category", cat.getId(),
            "Created: " + cat.getName());
        return toCategoryResponse(cat);
    }

    @Transactional
    public CategoryResponse updateCategory(String adminEmail, Long id, CategoryRequest req) {
        Category cat = categoryRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Category not found"));
        cat.setName(req.name().trim());
        cat.setDescription(req.description());
        cat.setActive(req.active());
        cat = categoryRepo.save(cat);
        audit(adminEmail, "CATEGORY_UPDATED", "Category", id,
            "Updated: " + cat.getName());
        return toCategoryResponse(cat);
    }

    // ─── Audit Logs ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<AuditEventResponse> listAuditEvents(int page, int size) {
        Pageable pageable = PageRequest.of(
            Math.max(page, 0),
            Math.min(Math.max(size, 1), 100),
            Sort.by("createdAt").descending()
        );
        return auditRepo.findAll(pageable).map(this::toAuditEventResponse);
    }

    // ─── Admin Dashboard Stats ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse getAdminStats() {
        long totalUsers = userRepo.count();
        long totalFeedback = feedbackRepo.count();
        double avgRating = feedbackRepo.calculateAverageRating();
        long pending = feedbackRepo.countByStatus(FeedbackStatus.PENDING);
        long unresolvedAbuse = abuseFlagRepo.countByResolvedFalse();
        long totalCategories = categoryRepo.count();
        return new AdminDashboardStatsResponse(
            totalUsers, totalFeedback, avgRating, pending, unresolvedAbuse, totalCategories);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private User findUser(Long id) {
        return userRepo.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
    }

    private UserResponse toUserResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getRole(),
            u.isActive(), u.getCreatedAt());
    }

    private CategoryResponse toCategoryResponse(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
    }

    private AuditEventResponse toAuditEventResponse(AuditEvent e) {
        String actorEmail = e.getActor() != null ? e.getActor().getEmail() : "system";
        return new AuditEventResponse(
            e.getId(), actorEmail, e.getAction(),
            e.getEntityType(), e.getEntityId(), e.getDetails(), e.getCreatedAt());
    }

    private void audit(String adminEmail, String action,
                       String entityType, Long entityId, String details) {
        userRepo.findByEmailIgnoreCase(adminEmail).ifPresent(admin -> {
            auditRepo.save(new AuditEvent(admin, action, entityType, entityId, details));
        });
    }
}
