package com.asrevo.cvhome.podregistry.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.podregistry.commons.dto.PodView;
import com.asrevo.cvhome.podregistry.commons.errors.DuplicatePodNameException;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;

/** The pod registry: what pods exist, who owns them, and what state they are in. */
public interface PodService {

    /** Every pod, for the gateway's route table and for super-admin screens. */
    Page<Pod> listAllPods(Pageable pageable);

    /** The pods an organization owns privately. */
    Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable);

    /**
     * Pods a store may be placed on when its organization owns none. Never returns another organization's private
     * pod — see {@code PodRepository.findPlaceablePublicPods}.
     */
    List<Pod> listPlaceablePublicPods();

    /** @throws PodNotFoundException the id is not registered — a 404, where this used to answer 200 with null */
    Pod pod(PodId podId) throws PodNotFoundException;

    /** @throws PodNotFoundException the id is not registered */
    PodView view(PodId podId) throws PodNotFoundException;

    /** @throws DuplicatePodNameException a pod already holds that name */
    Pod save(Pod pod) throws DuplicatePodNameException;

    /**
     * @throws PodNotFoundException        the id is not registered
     * @throws DuplicatePodNameException   the new name is already held by a different pod
     */
    Pod update(PodId podId, Pod pod) throws PodNotFoundException, DuplicatePodNameException;

    /** @throws PodNotFoundException the id is not registered */
    void delete(PodId podId) throws PodNotFoundException;

}
