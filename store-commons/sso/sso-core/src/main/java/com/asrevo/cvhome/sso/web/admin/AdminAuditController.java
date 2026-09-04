package com.asrevo.cvhome.sso.web.admin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.sso.audit.AuditEventType;
import com.asrevo.cvhome.sso.audit.AuditQueryService;
import com.asrevo.cvhome.sso.dto.AuditEventDto;
import com.asrevo.cvhome.sso.dto.AuditQueryParams;
import com.asrevo.cvhome.sso.dto.AuditTypeDto;
import com.asrevo.cvhome.uaa.errors.AuditEventNotFoundException;
import com.asrevo.cvhome.uaa.errors.AuditExportTooLargeException;
import com.asrevo.cvhome.uaa.errors.AuditQueryInvalidException;

import lombok.RequiredArgsConstructor;

/** The audit log, read. Nothing here writes one: every row is a side effect of the action it describes. */
@RestController
@RequestMapping("/api/v1/admin/audit")
@RequiredArgsConstructor
public class AdminAuditController {

    private static final String ADMIN = "hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')";

    private static final String CSV = "text/csv;charset=UTF-8";

    private final AuditQueryService audit;

    @PreAuthorize(ADMIN)
    @GetMapping
    public Page<AuditEventDto> search(@ModelAttribute AuditQueryParams params,
                                      @PageableDefault(size = 50) Pageable pageable) throws AuditQueryInvalidException {
        return audit.search(params.toSearch(), pageable);
    }

    @PreAuthorize(ADMIN)
    @GetMapping("/types")
    public List<AuditTypeDto> types() {
        return List.of(AuditEventType.values()).stream().map(AuditTypeDto::of).toList();
    }

    @PreAuthorize(ADMIN)
    @GetMapping("{id}")
    public AuditEventDto findOne(@PathVariable long id) throws AuditEventNotFoundException {
        return audit.findOne(id);
    }

    /**
     * The same query as CSV, streamed. A browser downloads it from a plain link, which is why it is a GET and why
     * the session — not a bearer token — is what authorises it.
     */
    @PreAuthorize(ADMIN)
    @GetMapping("/export")
    public void export(@ModelAttribute AuditQueryParams params, HttpServletResponse response)
            throws AuditQueryInvalidException, AuditExportTooLargeException, IOException {
        response.setContentType(CSV);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"uaa-audit.csv\"");
        try (PrintWriter writer = response.getWriter()) {
            audit.exportCsv(params.toSearch(), writer);
        }
    }

}
