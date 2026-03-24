package com.asrevo.cvhome.controlplane.org.config;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.org.entity.PodEntity;
import com.asrevo.cvhome.controlplane.org.repository.PodRepository;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PodDatabaseInitializer {

	private final ServiceDomainProperties properties;

	private final PodRepository podRepository;

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
			getById(pod).ifPresentOrElse(entity -> {
				log.debug("Updating pod {} from properties", pod);
				updateEntity(entity, pod);
				podRepository.save(entity);
			}, () -> {
				log.info("Creating new pod {} from properties", pod);
				podRepository.save(PodEntity.newEntity(pod));
			});
		}
	}

	private @NonNull Optional<PodEntity> getById(Pod pod) {
		if (pod.id() == null) {
			return Optional.empty();
		}
		return podRepository.findById(pod.id());
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
