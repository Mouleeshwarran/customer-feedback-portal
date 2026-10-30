
package com.feedback.portal.controller;

import com.feedback.portal.dto.*;
import com.feedback.portal.service.AdminService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public Page<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(page, size);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return adminService.getUser(id);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest request,
            Principal principal) {
        UserResponse created = adminService.createUser(principal.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request,
            Principal principal) {
        return adminService.updateUser(principal.getName(), id, request);
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(
            @PathVariable Long id,
            Principal principal) {
        adminService.setUserActive(principal.getName(), id, true);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id,
            Principal principal) {
        adminService.setUserActive(principal.getName(), id, false);
        return ResponseEntity.noContent().build();
    }
}
