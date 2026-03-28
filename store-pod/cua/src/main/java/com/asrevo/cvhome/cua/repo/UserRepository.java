package com.asrevo.cvhome.cua.repo;

import com.asrevo.cvhome.cua.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

	Optional<User> findByClientIdAndUsername(String clientId, String username);

	Optional<User> findByClientIdAndEmail(String clientId, String email);

}
