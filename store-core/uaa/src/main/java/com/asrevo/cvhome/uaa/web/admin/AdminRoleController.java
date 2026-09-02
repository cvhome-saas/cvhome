package com.asrevo.cvhome.uaa.web.admin;

import java.util.List;
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

import com.asrevo.cvhome.uaa.dto.CreateRoleRequest;
import com.asrevo.cvhome.uaa.dto.PermissionDto;
import com.asrevo.cvhome.uaa.dto.RoleDto;
import com.asrevo.cvhome.uaa.dto.UpdateRoleRequest;
import com.asrevo.cvhome.uaa.errors.DuplicateRoleNameException;
import com.asrevo.cvhome.uaa.errors.PermissionUnknownException;
import com.asrevo.cvhome.uaa.errors.RoleInUseException;
import com.asrevo.cvhome.uaa.errors.RoleInheritanceCycleException;
import com.asrevo.cvhome.uaa.errors.RoleNameInvalidException;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.errors.SystemRoleImmutableException;
import com.asrevo.cvhome.uaa.service.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping
    public Page<RoleDto> roles(@PageableDefault Pageable pageable) {
        return roleService.findAll(pageable);
    }

    /** The catalogue every role picks from; static, so the form cannot offer a key the server would refuse. */
    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("permissions")
    public List<PermissionDto> permissions() {
        return RoleService.catalogue();
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @GetMapping("{id}")
    public RoleDto role(@PathVariable UUID id) throws RoleNotFoundException {
        return roleService.findOne(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id)
            throws RoleNotFoundException, SystemRoleImmutableException, RoleInUseException {
        roleService.delete(id);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PostMapping
    public RoleDto create(@RequestBody CreateRoleRequest request)
            throws DuplicateRoleNameException, PermissionUnknownException, RoleNotFoundException,
            RoleInheritanceCycleException, RoleNameInvalidException {
        return roleService.create(request);
    }

    @PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
    @PutMapping("{id}")
    public RoleDto update(@PathVariable UUID id, @RequestBody UpdateRoleRequest request)
            throws RoleNotFoundException, SystemRoleImmutableException, DuplicateRoleNameException,
            PermissionUnknownException, RoleInheritanceCycleException, RoleNameInvalidException {
        return roleService.update(id, request);
    }

}
