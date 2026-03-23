package com.asrevo.cvhome.controlplane.org.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.org.repository.PodRepository;
import com.asrevo.cvhome.controlplane.org.service.PodService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PodServiceImpl implements PodService {

	private final PodRepository podRepository;

	@Override
	public List<Pod> listAllPods() {
		return podRepository.listAllPods();
	}

	@Override
	public List<Pod> listAllPods(ManagerOrgId orgId) {
		return podRepository.listAllPods(orgId);
	}

	@Override
	public List<Pod> listPublicPods() {
		return podRepository.listAllPods(null);
	}

	@Override
	public Pod pod(PodId podId) {
		return podRepository.pod(podId);
	}

}
