
package com.feedback.portal.controller;

import com.feedback.portal.dto.AuditEventResponse;
import com.feedback.portal.dto.AdminDashboardStatsResponse;
import com.feedback.portal.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminService adminService;

    public AdminDashboardController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public AdminDashboardStatsResponse getAdminStats() {
        return adminService.getAdminStats();
    }

    @GetMapping("/audit")
    public Page<AuditEventResponse> getAuditLog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminService.listAuditEvents(page, size);
    }
}
