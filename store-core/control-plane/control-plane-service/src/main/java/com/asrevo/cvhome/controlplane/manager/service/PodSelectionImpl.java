package com.asrevo.cvhome.controlplane.manager.service;

import java.util.List;
import java.util.Random;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.controlplane.org.service.PodService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PodSelectionImpl implements PodSelection {

    private static final Random rnd = new Random();

    private final PodService podService;

    @Override
    public PodId next(ManagerOrgId orgId, PodId preferredPodId) {
        List<Pod> allPods = podService.listAllPods(Pageable.unpaged()).toList();
        log.info("querying {} namespaces to get valid for org {}", allPods.size(), orgId);

        List<Pod> myPrivatePods = podService.listAllPods(orgId, Pageable.unpaged()).toList();

        if (!myPrivatePods.isEmpty()) {
            log.info("find {} private namespaces valid for org {}", myPrivatePods.size(), orgId);
            return findPrivatePod(orgId, preferredPodId, myPrivatePods);
        }

        List<Pod> publicNamespaces = podService.listPublicPods();

        log.info("find {} public namespaces valid for org {}", publicNamespaces.size(), orgId);
        Pod pod = random(publicNamespaces);
        log.info("will select public pod {} for org {}", pod, orgId);
        return pod.id();
    }

    private PodId findPrivatePod(ManagerOrgId orgId, PodId preferredPodId, List<Pod> myPrivatePods) {
        Pod preferredPod = myPrivatePods.stream().filter(it -> it.id().equals(preferredPodId)).findFirst().orElse(null);
        if (preferredPod != null) {
            log.info("will select private preferred pod {} for org {}", preferredPod, orgId);
            return preferredPod.id();
        } else {
            log.info("will select private pod for org {} from {}", orgId, myPrivatePods);
            Pod pod = random(myPrivatePods);
            log.info("will select private pod {} for org {}", pod, orgId);
            return pod.id();
        }
    }

    private Pod random(List<Pod> namespaces) {
        return namespaces.get(rnd.nextInt(namespaces.size()));
    }

}
