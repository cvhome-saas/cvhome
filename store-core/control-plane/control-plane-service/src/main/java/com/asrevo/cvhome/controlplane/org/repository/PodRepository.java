package com.asrevo.cvhome.controlplane.org.repository;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.org.entity.PodEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PodRepository extends ListCrudRepository<PodEntity, PodId> {

	List<PodEntity> findAllByOrgId(ManagerOrgId orgId);

	default List<Pod> listAllPods() {
		return findAll().stream().map(PodEntity::toPod).toList();
	}

	default List<Pod> listAllPods(ManagerOrgId orgId) {
		return findAllByOrgId(orgId).stream().map(PodEntity::toPod).toList();
	}

	default Pod pod(PodId podId) {
		return findById(podId).map(PodEntity::toPod).orElse(null);
	}

}
