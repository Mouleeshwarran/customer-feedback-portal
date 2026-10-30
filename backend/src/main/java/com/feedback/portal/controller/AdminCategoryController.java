
package com.feedback.portal.controller;

import com.feedback.portal.dto.*;
import com.feedback.portal.service.AdminService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final AdminService adminService;

    public AdminCategoryController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Page<CategoryResponse> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listCategories(page, size);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {
        CategoryResponse created = adminService.createCategory(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public CategoryResponse updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            Principal principal) {
        return adminService.updateCategory(principal.getName(), id, request);
    }
}
