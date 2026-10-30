
package com.feedback.portal.controller;

import com.feedback.portal.dto.CategoryResponse;
import com.feedback.portal.service.AdminService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final AdminService adminService;

    public CategoryController(AdminService adminService) {
        this.adminService = adminService;
    }

    /** Public endpoint — returns only active categories for use in forms. */
    @GetMapping
    public List<CategoryResponse> listActiveCategories() {
        return adminService.listActiveCategories();
    }
}
