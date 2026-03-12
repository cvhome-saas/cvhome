package com.asrevo.cvhome.uaa.repo;

import com.asrevo.cvhome.uaa.domain.SigningKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SigningKeyRepository extends JpaRepository<SigningKey, UUID> {

	Optional<SigningKey> findFirstByActiveTrueOrderByCreatedAtDesc();

	List<SigningKey> findTop5ByOrderByCreatedAtDesc();

	Optional<SigningKey> findByKid(String kid);

}
