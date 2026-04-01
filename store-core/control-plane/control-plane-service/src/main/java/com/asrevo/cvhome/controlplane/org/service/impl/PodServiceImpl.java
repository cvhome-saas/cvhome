package com.asrevo.cvhome.controlplane.org.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.org.entity.PodEntity;
import com.asrevo.cvhome.controlplane.org.repository.PodRepository;
import com.asrevo.cvhome.controlplane.org.service.PodService;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PodServiceImpl implements PodService {

	private final PodRepository podRepository;

	@Override
	public Page<Pod> listAllPods(Pageable pageable) {
		return podRepository.listAllPods(pageable);
	}

	@Override
	public Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable) {
		return podRepository.listAllPods(orgId, pageable);
	}

	@Override
	public List<Pod> listPublicPods() {
		return podRepository.listAllPods(Pageable.unpaged()).toList();
	}

	@Override
	public Pod pod(PodId podId) {
		return podRepository.pod(podId);
	}

	@Override
	public Pod save(Pod pod) {
		podRepository.findByName(pod.name()).ifPresent(p -> {
			if (!p.getId().equals(pod.id())) {
				throw new RuntimeException("Pod name must be unique");
			}
		});
		PodEntity entity = PodEntity.newEntity(pod);
		return podRepository.save(entity).toPod();
	}

	@Override
	public void delete(PodId podId) {
		podRepository.deleteById(podId);
	}

	@Transactional
	@Override
	public Pod update(PodId id, Pod pod) {
		PodEntity entity = podRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Pod not found"));
		if (pod.name() != null) {
			entity.setName(pod.name());
		}
		if (pod.endpoint() != null) {
			if (pod.endpoint().endpoint() != null) {
				entity.setEndpoint(pod.endpoint().endpoint());
			}
			if (pod.endpoint().type() != null) {
				entity.setEndpointType(pod.endpoint().type());
			}
		}
		if (pod.orgId() != null) {
			entity.setOrgId(pod.orgId());
		}

		return podRepository.save(entity).toPod();
	}

}
