package com.asrevo.cvhome.uaa.web.admin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.errors.RoleNotAssignableException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SuperAdminImmutableException;
import com.asrevo.cvhome.uaa.errors.UserNotFoundException;
import com.asrevo.cvhome.uaa.service.AdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminService adminService;

    private static @NonNull Map<String, String> extractMetadataFilters(Map<String, String> allParams) {
        Map<String, String> metadataFilters = new java.util.HashMap<>();
        allParams.forEach((k, v) -> {
            final String METADATA_PREFIX = "metadata[";
            if (k.startsWith(METADATA_PREFIX)) {
                String key = k.substring(METADATA_PREFIX.length(), k.length() - 1);
                metadataFilters.put(key, v);
            }
        });
        return metadataFilters;
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public Page<UserDto> users(@RequestParam Map<String, String> allParams, @PageableDefault Pageable pageable) {
        // Extract metadata filters from all query params, e.g. metadata[tenant]=acme
        return adminService.getUsers(extractMetadataFilters(allParams), pageable);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public UserDto user(@PathVariable UUID id) throws UserNotFoundException {
        return adminService.getUser(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("/exists")
    public boolean usernameExist(@RequestParam String username) {
        return adminService.usernameExist(username);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/enable")
    public void enable(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.enableUser(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/disable")
    public void disable(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.disableUser(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) throws UserNotFoundException, SuperAdminImmutableException {
        adminService.delete(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public UserDto create(@RequestBody CreateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        return adminService.createUser(req);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @RequestBody UpdateUserRequest req)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        return adminService.updateUser(id, req);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable UUID id, @RequestBody ResetUserPasswordRequest req)
            throws UserNotFoundException, SuperAdminImmutableException {
        adminService.resetPassword(id, req);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/roles")
    public void assign(@PathVariable UUID id, @RequestBody Set<String> roles)
            throws UserNotFoundException, SuperAdminImmutableException, RoleNotFoundException, RoleNotAssignableException {
        adminService.assignRoles(id, roles);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping("/{id}/roles/remove")
    public void removeRoles(@PathVariable UUID id, @RequestBody Set<String> roles)
            throws UserNotFoundException, SuperAdminImmutableException {
        adminService.removeRoles(id, roles);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("/assignable-roles")
    public Set<String> assignableRoles() {
        return adminService.getAssignableRoles();
    }

}
