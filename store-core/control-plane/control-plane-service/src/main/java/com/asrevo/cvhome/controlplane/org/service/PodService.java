package com.asrevo.cvhome.controlplane.org.service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PodService {

	Page<Pod> listAllPods(Pageable pageable);

	Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable);

	List<Pod> listPublicPods();

	Pod pod(PodId podId);

	Pod save(Pod pod);

	void delete(PodId podId);

}
