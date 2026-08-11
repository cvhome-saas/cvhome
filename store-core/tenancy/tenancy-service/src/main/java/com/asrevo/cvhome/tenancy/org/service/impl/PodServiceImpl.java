package com.asrevo.cvhome.tenancy.org.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.tenancy.errors.PodNotFoundException;
import com.asrevo.cvhome.tenancy.org.entity.PodEntity;
import com.asrevo.cvhome.tenancy.org.repository.PodRepository;
import com.asrevo.cvhome.tenancy.org.service.PodService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
                throw new IllegalArgumentException("Pod name must be unique");
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
    public Pod update(PodId id, Pod pod) throws PodNotFoundException {
        PodEntity entity = podRepository.findById(id).orElseThrow(() -> PodNotFoundException.of(id));
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
