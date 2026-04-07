package com.asrevo.cvhome.controlplane.org.service.impl;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.org.service.PodService;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Component responsible for initializing pods from configuration on application startup.
 * This replaces the static SQL INSERT for pods with dynamic runtime initialization.
 * Uses ApplicationReadyEvent to ensure the application is fully ready before initializing pods.
 */
@Component
@AllArgsConstructor
@Slf4j
public class PodInitializationRunner implements ApplicationListener<ApplicationReadyEvent> {

    private final PodService podService;
    private final ServiceDomainProperties serviceDomainProperties;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Starting pod initialization from configuration...");

        if (serviceDomainProperties.pods() == null || serviceDomainProperties.pods().isEmpty()) {
            log.warn("No pods configured in ServiceDomainProperties. Skipping pod initialization.");
            return;
        }

        int initializedCount = 0;
        int updatedCount = 0;

        for (Pod pod : serviceDomainProperties.pods()) {
            try {
                // Try to find existing pod by ID
                Pod existingPod = podService.pod(pod.id());

                if (existingPod != null) {
                    // Update existing pod
                    podService.update(pod.id(), pod);
                    updatedCount++;
                    log.debug("Updated existing pod: {}", pod.name());
                } else {
                    // Insert new pod
                    podService.save(pod);
                    initializedCount++;
                    log.debug("Initialized new pod: {}", pod.name());
                }
            } catch (Exception e) {
                log.error("Failed to initialize/update pod {}: {}", pod.name(), e.getMessage(), e);
                // Continue with other pods instead of failing completely
            }
        }

        log.info("Pod initialization completed. Initialized: {}, Updated: {}", initializedCount, updatedCount);
    }
}
