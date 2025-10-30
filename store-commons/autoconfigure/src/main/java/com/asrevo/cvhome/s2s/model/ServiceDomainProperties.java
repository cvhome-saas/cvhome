package com.asrevo.cvhome.s2s.model;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome")
public record ServiceDomainProperties(Map<String, ServiceDomain> services, List<Pod> pods) {

	public Optional<Pod> getPodByPodId(PodId id) {
		return this.pods.stream().filter(it -> id.equals(it.id())).findFirst();
	}

	public ServiceDomain getService(String serviceName) {
		return this.services.get(serviceName);
	}
}
