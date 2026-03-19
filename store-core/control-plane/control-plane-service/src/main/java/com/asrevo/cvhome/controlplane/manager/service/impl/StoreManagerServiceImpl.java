package com.asrevo.cvhome.controlplane.manager.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.controlplane.manager.mappers.ManagerStoreMappers;
import com.asrevo.cvhome.controlplane.manager.service.InternalStoreService;
import com.asrevo.cvhome.controlplane.manager.service.PodSelection;
import com.asrevo.cvhome.controlplane.manager.service.StoreManagerService;
import com.asrevo.cvhome.controlplane.manager.service.StorePodClientFactory;
import com.asrevo.cvhome.merchant.api.MerchantStorePodClient;

import java.util.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class StoreManagerServiceImpl implements StoreManagerService {

	private final InternalStoreService internalStoreService;

	private final ManagerStoreMappers managerStoreMappers;

	private final StorePodClientFactory podClientFactory;

	private final PodSelection podSelection;

	public StoreManagerServiceImpl(InternalStoreService internalStoreService, ManagerStoreMappers managerStoreMappers,
			StorePodClientFactory podClientFactory, PodSelection podSelection) {
		this.internalStoreService = internalStoreService;
		this.managerStoreMappers = managerStoreMappers;
		this.podClientFactory = podClientFactory;
		this.podSelection = podSelection;
	}

	@Override
	public ManagerStoreDto createStore(ManagerOrgId orgId, Map<Object, Object> request) {
		PodId prefaredPodId = Optional.ofNullable(request.get("pod"))
			.map(it -> ((Map<String, String>) it))
			.filter(it -> it.containsKey("id"))
			.map(it -> it.get("id"))
			.filter(it -> !it.trim().isEmpty())
			.map(PodId::new)
			.orElse(null);
		PodId podId = podSelection.next(orgId, prefaredPodId);
		return internalStoreService.createStore(request, orgId, podId);
	}

	@Override
	public Mono<PageImpl<Object>> findAll(UserOrgStoreIdentity identity, ListManagerStoreQuery listManagerStoreQuery,
			Pageable pageable) {
		Page<ManagerStoreDto> internalStores = internalStoreService.findAll(identity, listManagerStoreQuery, pageable);
		Mono<List<Object>> listMono = Flux.fromIterable(internalStores.getContent())
			.flatMap(it -> getStore(it.id()).onErrorResume(e -> Mono.empty()))
			.collectList();
		return listMono.map(it -> managerStoreMappers.toPage(it, internalStores));
	}

	@Override
	public Mono<Object> getStore(ManagerStoreId managerStoreId) {
		Pod pod = internalStoreService.getStorePod(managerStoreId);
		MerchantStorePodClient client = podClientFactory.getMerchantStorePodClient(pod.id());
		return client.getStore(managerStoreId.getId().toString()).flatMap(response -> {
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				HashMap<String, Object> newIt = new HashMap<>(response.getBody());
				newIt.put("pod", Map.of("id", pod.id().id()));
				return Mono.just((Object) newIt);
			}
			String errorMessage = "Failed to fetch store from pod: " + response.getStatusCode();
			if (response.getBody() != null) {
				errorMessage += " - Details: " + response.getBody();
			}
			return Mono.error(new RuntimeException(errorMessage));
		});
	}

}
