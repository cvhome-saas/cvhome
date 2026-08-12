package com.asrevo.cvhome.podregistry.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.dto.PodView;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.domain.PodEntity;
import com.asrevo.cvhome.podregistry.repository.PodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PodServiceImpl implements PodService {

    private final PodRepository podRepository;

    @Override
    public Page<Pod> listAllPods(Pageable pageable) {
        return podRepository.findAll(pageable).map(PodEntity::toPod);
    }

    @Override
    public Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable) {
        return podRepository.findAllByOrgId(orgId, pageable).map(PodEntity::toPod);
    }

    @Override
    public List<Pod> listPlaceablePublicPods() {
        return podRepository.findPlaceablePublicPods().stream().map(PodEntity::toPod).toList();
    }

    @Override
    public Pod pod(PodId podId) throws PodNotFoundException {
        return entity(podId).toPod();
    }

    @Override
    public PodView view(PodId podId) throws PodNotFoundException {
        return entity(podId).toView();
    }

    private PodEntity entity(PodId podId) throws PodNotFoundException {
        return podRepository.findById(podId).orElseThrow(() -> PodNotFoundException.of(podId));
    }

    /**
     * The unique-name check and the insert are one transaction, and the constraint is still the authority.
     *
     * <p>
     * Checking first gives the caller a typed 409 naming the pod instead of a raw constraint violation, but two
     * instances can both pass that check, so {@code pod_name_uq} is caught and translated to the same exception. The
     * check is for the message; the constraint is for correctness.
     * </p>
     */
    @Override
    @Transactional
    public Pod save(Pod pod) throws DuplicatePodNameException {
        if (podRepository.findByName(pod.name()).isPresent()) {
            throw DuplicatePodNameException.of(pod.name());
        }
        return persist(PodEntity.newEntity(pod), pod.name());
    }

    @Override
    @Transactional
    public Pod update(PodId podId, Pod pod) throws PodNotFoundException, DuplicatePodNameException {
        PodEntity entity = entity(podId);
        if (Objects.nonNull(pod.name()) && !pod.name().equals(entity.getName())) {
            if (podRepository.findByName(pod.name()).filter(it -> !it.getId().equals(podId)).isPresent()) {
                throw DuplicatePodNameException.of(pod.name());
            }
            entity.setName(pod.name());
        }
        if (Objects.nonNull(pod.endpoint())) {
            entity.setEndpoint(pod.endpoint().endpoint());
            entity.setEndpointType(pod.endpoint().type());
        }
        return persist(entity, pod.name());
    }

    private Pod persist(PodEntity entity, String name) throws DuplicatePodNameException {
        try {
            return podRepository.save(entity).toPod();
        } catch (DuplicateKeyException e) {
            // Lost the race against another instance between the check above and this write.
            throw DuplicatePodNameException.of(name);
        }
    }

    @Override
    @Transactional
    public void delete(PodId podId) throws PodNotFoundException {
        podRepository.delete(entity(podId));
        log.info("Deleted pod {}", podId);
    }

}
