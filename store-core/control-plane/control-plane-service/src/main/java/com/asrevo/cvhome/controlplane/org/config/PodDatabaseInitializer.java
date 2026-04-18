package com.asrevo.cvhome.controlplane.org.config;

import java.util.List;
import java.util.Objects;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.org.entity.PodEntity;
import com.asrevo.cvhome.controlplane.org.service.PodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class PodDatabaseInitializer {

    private final ServiceDomainProperties properties;

    private final PodService podService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onApplicationReady() {
        List<Pod> pods = properties.pods();
        if (pods == null || pods.isEmpty()) {
            log.info("No pods found in properties to sync");
            return;
        }

        log.info("Syncing {} pods from properties to database", pods.size());

        for (Pod pod : pods) {
            if (Objects.nonNull(podService.pod(pod.id()))) {
                podService.update(pod.id(), pod);
            } else {
                log.info("Creating new pod {} from properties", pod);
                podService.save(pod);
            }
        }
    }

    private void updateEntity(PodEntity entity, Pod pod) {
        entity.setName(pod.name());
        if (pod.endpoint() != null) {
            entity.setEndpoint(pod.endpoint().endpoint());
            entity.setEndpointType(pod.endpoint().type());
        }
        entity.setOrgId(pod.orgId());
    }

}
