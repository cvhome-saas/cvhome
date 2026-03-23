package com.asrevo.cvhome.controlplane.org.service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;

import java.util.List;

public interface PodService {

	List<Pod> listAllPods();

	List<Pod> listAllPods(ManagerOrgId orgId);

	List<Pod> listPublicPods();

	Pod pod(PodId podId);

	Pod save(Pod pod);

	void delete(PodId podId);

}
