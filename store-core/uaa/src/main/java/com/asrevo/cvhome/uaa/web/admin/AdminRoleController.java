package com.asrevo.cvhome.uaa.web.admin;

import java.util.UUID;

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
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.dto.CreateRoleRequest;
import com.asrevo.cvhome.uaa.dto.UpdateRoleRequest;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.service.RoleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    public Role role(@PathVariable UUID id) throws RoleNotFoundException {
        return roleService.findBy(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id) throws RoleNotFoundException {
        roleService.delete(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public Role create(@RequestBody CreateRoleRequest request) {
        return roleService.create(request);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public Role update(@PathVariable UUID id, @RequestBody UpdateRoleRequest request) throws RoleNotFoundException {
        return roleService.update(id, request);
    }

}
