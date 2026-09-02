package com.asrevo.cvhome.uaa.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.uaa.domain.Role;
import com.asrevo.cvhome.uaa.dto.CreateRoleRequest;
import com.asrevo.cvhome.uaa.dto.UpdateRoleRequest;
import com.asrevo.cvhome.uaa.errors.RoleNotFoundException;
import com.asrevo.cvhome.uaa.repo.RoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;

    public Page<Role> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    public Role findBy(UUID id) throws RoleNotFoundException {
        return roleRepository.findById(id).orElseThrow(() -> RoleNotFoundException.of(id));
    }

    @Transactional
    public Role create(CreateRoleRequest request) {
        Role r = new Role();
        r.setName(request.name());
        return roleRepository.save(r);
    }

    @Transactional
    public Role update(UUID id, UpdateRoleRequest request) throws RoleNotFoundException {
        Role role = findBy(id);
        role.setName(request.name());
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(UUID id) throws RoleNotFoundException {
        roleRepository.delete(findBy(id));
    }

}
