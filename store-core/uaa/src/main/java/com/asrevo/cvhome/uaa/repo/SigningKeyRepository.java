package com.asrevo.cvhome.uaa.repo;

import com.asrevo.cvhome.uaa.domain.SigningKey;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SigningKeyRepository extends JpaRepository<SigningKey, UUID> {

	Optional<SigningKey> findFirstByActiveTrueOrderByCreatedAtDesc();

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT sk FROM SigningKey sk WHERE sk.active = true")
	List<SigningKey> findAllActiveWithLock();

	List<SigningKey> findTop5ByOrderByCreatedAtDesc();

}
