package com.asrevo.cvhome.uaa.web.admin;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.dto.CreateRoleRequest;
import com.asrevo.cvhome.uaa.dto.UpdateRoleRequest;
import com.asrevo.cvhome.uaa.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Slf4j
public class AdminRoleController {

	private final RoleService roleService;

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@GetMapping
	public Page<Role> roles(@PageableDefault Pageable pageable) {
		return roleService.findAll(pageable);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@GetMapping("{id}")
	public Role role(@PathVariable UUID id) {
		return roleService.findBy(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@DeleteMapping("{id}")
	public void delete(@PathVariable UUID id) {
		roleService.delete(id);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PostMapping
	public Role create(@RequestBody CreateRoleRequest request) {
		return roleService.create(request);
	}

	@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
	@PutMapping("/{id}")
	public Role update(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) {
		return roleService.update(id, request);
	}

}
