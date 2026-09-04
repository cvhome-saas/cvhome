package com.asrevo.cvhome.sso.web.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.dashboard.DashboardService;
import com.asrevo.cvhome.sso.dto.Dashboard;
import com.asrevo.cvhome.uaa.errors.AuditQueryInvalidException;

import lombok.RequiredArgsConstructor;

/** The overview screen's one read. */
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private final DashboardService dashboard;

    @PreAuthorize(ADMIN)
    @GetMapping
    public Dashboard get(@RequestParam(defaultValue = "24h") String range) throws AuditQueryInvalidException {
        return dashboard.of(range);
    }

}
