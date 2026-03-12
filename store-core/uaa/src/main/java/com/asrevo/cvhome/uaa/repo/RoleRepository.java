package com.asrevo.cvhome.uaa.repo;

import com.asrevo.cvhome.uaa.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

	Optional<Role> findByName(String name);

}
