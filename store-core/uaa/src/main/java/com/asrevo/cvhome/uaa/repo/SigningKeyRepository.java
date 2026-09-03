package com.asrevo.cvhome.uaa.repo;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.uaa.domain.SigningKey;
import com.asrevo.cvhome.uaa.domain.SigningKeyStatus;

public interface SigningKeyRepository extends JpaRepository<SigningKey, UUID> {

    List<SigningKey> findByStatus(SigningKeyStatus status);

    List<SigningKey> findByStatusIn(List<SigningKeyStatus> statuses);

    List<SigningKey> findAllByOrderByCreatedAtDesc();

    /** The active key, locked: rotation and bootstrap must not race into two of them. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select k from SigningKey k where k.status = com.asrevo.cvhome.uaa.domain.SigningKeyStatus.ACTIVE")
    List<SigningKey> findActiveWithLock();

}
