package com.asrevo.cvhome.sso.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.sso.domain.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(String name);

    @Query("select count(u) from User u join u.roles r where r.id = :roleId")
    long countHolders(UUID roleId);

}
