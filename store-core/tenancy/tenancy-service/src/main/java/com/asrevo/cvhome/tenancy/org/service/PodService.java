package com.asrevo.cvhome.tenancy.org.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.tenancy.errors.PodNotFoundException;

public interface PodService {

    Page<Pod> listAllPods(Pageable pageable);

    Page<Pod> listAllPods(ManagerOrgId orgId, Pageable pageable);

    List<Pod> listPublicPods();

    Pod pod(PodId podId);

    Pod save(Pod pod);

    void delete(PodId podId);

    Pod update(PodId id, Pod pod) throws PodNotFoundException;

}
