package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.PolicyVersion;
import com.asrevo.cvhome.content.model.PolicyVersionStatus;

public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, Long> {

    List<PolicyVersion> findByContentIdOrderByVersionDesc(Long contentId);

    Optional<PolicyVersion> findByContentIdAndVersion(Long contentId, Integer version);

    Optional<PolicyVersion> findFirstByContentIdAndStatus(Long contentId, PolicyVersionStatus status);

    void deleteByContentId(Long contentId);

}
