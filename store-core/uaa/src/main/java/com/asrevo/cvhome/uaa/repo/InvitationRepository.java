package com.asrevo.cvhome.uaa.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.uaa.domain.Invitation;
import com.asrevo.cvhome.uaa.domain.InvitationStatus;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    Optional<Invitation> findByTokenHash(String tokenHash);

    Optional<Invitation> findByUserIdAndStatus(UUID userId, InvitationStatus status);

    Page<Invitation> findByStatusOrderByCreatedAtDesc(InvitationStatus status, Pageable pageable);

    Page<Invitation> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Invitation> findByUserId(UUID userId);

}
