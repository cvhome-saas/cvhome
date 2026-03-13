package com.asrevo.cvhome.uaa.web.admin;

import com.asrevo.cvhome.uaa.dto.CreateUserRequest;
import com.asrevo.cvhome.uaa.dto.ResetUserPasswordRequest;
import com.asrevo.cvhome.uaa.dto.UpdateUserRequest;
import com.asrevo.cvhome.uaa.dto.UserDto;
import com.asrevo.cvhome.uaa.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

	private final AdminService adminService;

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@GetMapping
	public Page<UserDto> users(@RequestParam Map<String, String> allParams, @PageableDefault Pageable pageable) {
		// Extract metadata filters from all query params, e.g. metadata[tenant]=acme
		java.util.Map<String, String> metadataFilters = new java.util.HashMap<>();
		allParams.forEach((k, v) -> {
			if (k.startsWith("metadata[")) {
				String key = k.substring("metadata[".length(), k.length() - 1);
				metadataFilters.put(key, v);
			}
		});
		return adminService.getUsers(metadataFilters, pageable);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@GetMapping("/{id}")
	public UserDto user(@PathVariable UUID id) {
		return adminService.getUser(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@GetMapping("/exists")
	public boolean usernameExist(@RequestParam String username) {
		return adminService.usernameExist(username);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping("/{id}/enable")
	public void enable(@PathVariable UUID id) {
		adminService.enableUser(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping("/{id}/disable")
	public void disable(@PathVariable UUID id) {
		adminService.disableUser(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@DeleteMapping("/{id}")
	public void delete(@PathVariable UUID id) {
		adminService.delete(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping
	public UserDto create(@RequestBody CreateUserRequest req) {
		return adminService.createUser(req);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PutMapping("/{id}")
	public UserDto update(@PathVariable UUID id, @RequestBody UpdateUserRequest req) {
		return adminService.updateUser(id, req);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PutMapping("/{id}/reset-password")
	public void resetPassword(@PathVariable UUID id, @RequestBody ResetUserPasswordRequest req) {
		adminService.resetPassword(id, req);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping("/{id}/roles")
	public void assign(@PathVariable UUID id, @RequestBody Set<String> roles) {
		adminService.assignRoles(id, roles);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping("/{id}/roles/remove")
	public void removeRoles(@PathVariable UUID id, @RequestBody Set<String> roles) {
		adminService.removeRoles(id, roles);
	}

}
