package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import java.util.List;

public interface PodSelection {

	PodId next(ManagerOrgId orgId, PodId prefaredPodId);

	List<Pod> listPrivatePods(ManagerOrgId orgId);

	List<Pod> listAllPods();

}
