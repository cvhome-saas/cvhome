package com.asrevo.cvhome.uaa.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Permission;
import com.asrevo.cvhome.uaa.audit.AuditEventType;
import com.asrevo.cvhome.uaa.audit.AuditRecord;
import com.asrevo.cvhome.uaa.audit.AuditService;
import com.asrevo.cvhome.uaa.audit.AuditTargetType;
import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.domain.RoleScope;
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
import com.asrevo.cvhome.uaa.repo.RoleRepository;

import lombok.RequiredArgsConstructor;

/**
 * Roles: names every service authorises on, carrying the permissions the token will describe.
 *
 * <p>
 * A system role's name and scope are fixed and it cannot be deleted — services key on the name — but its description,
 * parent and permissions are data and stay editable. Every write is audited with the before/after of the DTO.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    static final Pattern NAME = Pattern.compile("^[A-Z][A-Z0-9_]{1,79}$");

    private final RoleRepository roleRepository;

    private final AuditService audit;

    @Transactional(readOnly = true)
    public Page<RoleDto> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public RoleDto findOne(UUID id) throws RoleNotFoundException {
        return toDto(findBy(id));
    }

    Role findBy(UUID id) throws RoleNotFoundException {
        return roleRepository.findById(id).orElseThrow(() -> RoleNotFoundException.of(id));
    }

    public static List<PermissionDto> catalogue() {
        return Arrays.stream(Permission.values())
                .map(p -> new PermissionDto(p.key(), p.group().name(), p.description()))
                .toList();
    }

    @Transactional
    public RoleDto create(CreateRoleRequest request)
            throws DuplicateRoleNameException, PermissionUnknownException, RoleNotFoundException,
            RoleInheritanceCycleException, RoleNameInvalidException {
        String name = validName(request.name());
        if (roleRepository.findByName(name).isPresent()) {
            throw DuplicateRoleNameException.of(name);
        }
        Role role = new Role(name);
        role.setDescription(request.description());
        role.setScope(request.scope() == null ? RoleScope.REALM : request.scope());
        role.setPermissions(validPermissions(request.permissions()));
        if (request.inheritsFromId() != null) {
            role.setInheritsFrom(parentFor(role, request.inheritsFromId()));
        }
        Role saved = roleRepository.save(role);
        RoleDto dto = toDto(saved);
        audit.record(AuditRecord.of(AuditEventType.ROLE_CREATED).target(AuditTargetType.ROLE, dto.id().toString(), name)
                .change(null, dto));
        return dto;
    }

    @Transactional
    public RoleDto update(UUID id, UpdateRoleRequest request)
            throws RoleNotFoundException, SystemRoleImmutableException, DuplicateRoleNameException,
            PermissionUnknownException, RoleInheritanceCycleException, RoleNameInvalidException {
        Role role = findBy(id);
        RoleDto before = toDto(role);
        applyIdentity(role, request);
        if (request.description() != null) {
            role.setDescription(request.description());
        }
        applyParent(role, request);
        boolean permissionsChanged = applyPermissions(role, request);
        RoleDto after = toDto(roleRepository.save(role));
        audit.record(AuditRecord.of(permissionsChanged ? AuditEventType.ROLE_PERMISSIONS_UPDATED : AuditEventType.ROLE_UPDATED)
                .target(AuditTargetType.ROLE, id.toString(), after.name())
                .change(before, after));
        return after;
    }

    /** Name and scope: the contract other services key on, so a system role refuses both. */
    private void applyIdentity(Role role, UpdateRoleRequest request)
            throws SystemRoleImmutableException, DuplicateRoleNameException, RoleNameInvalidException {
        if (request.name() != null && !request.name().equals(role.getName())) {
            if (role.isSystemRole()) {
                throw SystemRoleImmutableException.of(role.getName());
            }
            String name = validName(request.name());
            if (roleRepository.findByName(name).isPresent()) {
                throw DuplicateRoleNameException.of(name);
            }
            role.setName(name);
        }
        if (request.scope() != null && request.scope() != role.getScope()) {
            if (role.isSystemRole()) {
                throw SystemRoleImmutableException.of(role.getName());
            }
            role.setScope(request.scope());
        }
    }

    private void applyParent(Role role, UpdateRoleRequest request)
            throws RoleNotFoundException, RoleInheritanceCycleException {
        if (Boolean.TRUE.equals(request.clearInheritsFrom())) {
            role.setInheritsFrom(null);
        } else if (request.inheritsFromId() != null) {
            role.setInheritsFrom(parentFor(role, request.inheritsFromId()));
        }
    }

    private static boolean applyPermissions(Role role, UpdateRoleRequest request) throws PermissionUnknownException {
        if (request.permissions() == null) {
            return false;
        }
        Set<String> next = validPermissions(request.permissions());
        boolean changed = !next.equals(role.getPermissions());
        role.setPermissions(next);
        return changed;
    }

    @Transactional
    public void delete(UUID id) throws RoleNotFoundException, SystemRoleImmutableException, RoleInUseException {
        Role role = findBy(id);
        if (role.isSystemRole()) {
            throw SystemRoleImmutableException.of(role.getName());
        }
        long holders = roleRepository.countHolders(id);
        if (holders > 0) {
            throw RoleInUseException.of(role.getName(), holders);
        }
        RoleDto before = toDto(role);
        roleRepository.delete(role);
        audit.record(AuditRecord.of(AuditEventType.ROLE_DELETED).target(AuditTargetType.ROLE, id.toString(), role.getName())
                .change(before, null));
    }

    private Role parentFor(Role role, UUID parentId) throws RoleNotFoundException, RoleInheritanceCycleException {
        Role parent = findBy(parentId);
        if (parent.isOrInheritsFrom(role) || parent.getId().equals(role.getId())) {
            throw RoleInheritanceCycleException.of(role.getName(), parent.getName());
        }
        return parent;
    }

    private static String validName(String name) throws RoleNameInvalidException {
        String candidate = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
        if (!NAME.matcher(candidate).matches()) {
            throw RoleNameInvalidException.of(name);
        }
        return candidate;
    }

    private static Set<String> validPermissions(Set<String> keys) throws PermissionUnknownException {
        if (keys == null) {
            return new HashSet<>();
        }
        Set<String> unknown = new TreeSet<>();
        for (String key : keys) {
            if (Permission.fromKey(key).isEmpty()) {
                unknown.add(key);
            }
        }
        if (!unknown.isEmpty()) {
            throw PermissionUnknownException.of(unknown);
        }
        return new HashSet<>(keys);
    }

    RoleDto toDto(Role role) {
        Set<String> effective = new TreeSet<>();
        role.effectivePermissions().forEach(p -> effective.add(p.key()));
        Role parent = role.getInheritsFrom();
        return new RoleDto(role.getId(), role.getName(), role.getDescription(), role.getScope(), role.isSystemRole(),
                parent == null ? null : parent.getId(), parent == null ? null : parent.getName(),
                new TreeSet<>(role.getPermissions()), effective, roleRepository.countHolders(role.getId()),
                role.getCreatedAt(), role.getUpdatedAt());
    }

}
