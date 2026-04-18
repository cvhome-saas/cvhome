package com.asrevo.cvhome.cua.repo;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.cua.domain.SigningKey;

public interface SigningKeyRepository extends JpaRepository<SigningKey, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sk FROM SigningKey sk WHERE sk.active = true")
    List<SigningKey> findAllActiveWithLock();

    List<SigningKey> findTop5ByOrderByCreatedAtDesc();

}
