package com.asrevo.cvhome.podregistry.service;

import java.util.List;
import java.util.Objects;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private static final int DEFAULT_PAGE_SIZE = 100;

    private final PodRepository podRepository;

    @Override
    public Page<Pod> listAllPods(Pageable pageable) {
        return podRepository.findAll(pageable).map(PodEntity::toPod);
    }

    @Override
    public Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable) {
        return podRepository.findAllByOrgId(orgId, pageable).map(PodEntity::toPod);
    }

    /**
     * The count is a separate query because Spring Data JDBC's {@code @Query} has no {@code countQuery} attribute
     * — that is JPA's — so the page is assembled here, the same shape tenancy's store listing uses.
     *
     * <p>
     * A blank term is normalised to null rather than searched for. {@code ilike '%%'} happens to match everything,
     * but relying on that would make an empty box and a cleared box two different code paths for one behaviour.
     * </p>
     */
    @Override
    public Page<Pod> searchPods(ManagerOrgId orgId, String term, Pageable pageable) {
        Pageable page = pageable == null || pageable.isUnpaged() ? Pageable.ofSize(DEFAULT_PAGE_SIZE) : pageable;
        String org = orgId == null || orgId.getId() == null ? null : orgId.getId().toString();
        String search = StringUtils.hasText(term) ? term.trim() : null;
        List<Pod> rows = podRepository.findVisible(org, search, page.getPageSize(), page.getOffset()).stream()
                .map(PodEntity::toPod)
                .toList();
        return new PageImpl<>(rows, page, podRepository.countVisible(org, search));
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
