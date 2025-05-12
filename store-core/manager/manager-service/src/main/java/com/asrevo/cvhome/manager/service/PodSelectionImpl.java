package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.OrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@AllArgsConstructor
@Slf4j
public class PodSelectionImpl implements PodSelection {
    private final static Random rnd = new Random();
    private final ServiceDomainProperties properties;

    @Override
    public PodId next(OrgId orgId, PodId preferredPodId) {
        log.info("querying {} namespaces to get valid for org {}", properties.pods().size(), orgId);

        List<Pod> myPrivatePods = properties.pods().stream()
                .filter(it -> orgId.equals(it.orgId()))
                .toList();

        if (!myPrivatePods.isEmpty()) {
            log.info("find {} private namespaces valid for org {}", myPrivatePods.size(), orgId);
            return findPrivatePod(orgId, preferredPodId, myPrivatePods);
        }

        List<Pod> publicNamespaces = properties.pods().stream()
                .filter(it -> Objects.isNull(it.orgId()))
                .toList();

        log.info("find {} public namespaces valid for org {}", publicNamespaces.size(), orgId);
        Pod pod = random(publicNamespaces);
        log.info("will select public pod {} for org {}", pod, orgId);
        return pod.id();
    }

    private PodId findPrivatePod(OrgId orgId, PodId preferredPodId, List<Pod> myPrivatePods) {
        Pod preferredPod = myPrivatePods.stream().filter(it -> it.id().equals(preferredPodId)).findFirst().orElse(null);
        if (preferredPod != null) {
            log.info("will select private preferred pod {} for org {}", preferredPod, orgId);
            return preferredPod.id();
        } else {
            Pod pod = random(myPrivatePods);
            log.info("will select private pod {} for org {}", pod, orgId);
            return pod.id();

        }
    }

    private Pod random(List<Pod> namespaces) {
        return namespaces.get(rnd.nextInt(namespaces.size()));
    }
}
