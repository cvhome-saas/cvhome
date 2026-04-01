package com.asrevo.cvhome.controlplane.manager.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.controlplane.manager.service.InternalStoreService;
import com.asrevo.cvhome.controlplane.org.service.PodService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {

	public final InternalStoreService internalStoreService;

	private final PodService podService;

	@GetMapping("store-pod-by-store-id")
	@ConditionalOnApiStatus
	public Pod getStorePodByStoreId(@RequestParam ManagerStoreId store) {
		return podService.pod(internalStoreService.getStorePod(store));
	}

}
