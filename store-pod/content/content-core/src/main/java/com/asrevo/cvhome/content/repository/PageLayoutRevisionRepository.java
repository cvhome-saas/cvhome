package com.asrevo.cvhome.content.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.content.entity.PageLayoutRevision;

public interface PageLayoutRevisionRepository extends JpaRepository<PageLayoutRevision, Long> {

    List<PageLayoutRevision> findByLayoutIdOrderByVersionDesc(Long layoutId);

    Optional<PageLayoutRevision> findByLayoutIdAndVersion(Long layoutId, int version);

}
