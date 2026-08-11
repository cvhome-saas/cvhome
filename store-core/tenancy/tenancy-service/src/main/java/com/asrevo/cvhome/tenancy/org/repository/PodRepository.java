package com.asrevo.cvhome.tenancy.org.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.tenancy.org.entity.PodEntity;

@Repository
public interface PodRepository extends CrudRepository<PodEntity, PodId>, PagingAndSortingRepository<PodEntity, PodId> {

    Page<PodEntity> findAllByOrgId(ManagerOrgId orgId, Pageable pageable);

    Optional<PodEntity> findByName(String name);

    default Page<Pod> listAllPods(Pageable pageable) {
        return findAll(pageable).map(PodEntity::toPod);
    }

    default Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable) {
        return findAllByOrgId(orgId, pageable).map(PodEntity::toPod);
    }

    default Pod pod(PodId podId) {
        return findById(podId).map(PodEntity::toPod).orElse(null);
    }

}
