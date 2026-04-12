package com.asrevo.cvhome.merchant.api.v1;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.service.AskTlsService;
import com.asrevo.cvhome.merchant.service.LookupDomainHeadersService;
import com.asrevo.cvhome.merchant.service.RoutingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {

	public final RoutingService routingService;

	private final AskTlsService askTlsService;

	private final LookupDomainHeadersService lookupDomainHeadersService;

	@GetMapping("public/ask-for-tls")

	public ResponseEntity<Object> ask(Domain domain) {
		log.info("tls ask: {}", domain);
		if (askTlsService.ask(domain)) {
			return ResponseEntity.ok().build();
		}
		else {
			return ResponseEntity.badRequest().build();
		}
	}

	@GetMapping("public/lookup-by-domain")

	public Map<String, String> getLookupHeadersByDomain(Domain domain) {
		log.info("header lookup: {}", domain);
		return lookupDomainHeadersService.lookupHeaders(domain);
	}

	@GetMapping("private/allocates")

	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
	public Mono<Set<ManagerStoreDomain>> allocatedDomains(StoreMerchantId merchantStore) {
		return Mono.just(routingService.domains(merchantStore));
	}

	@PostMapping("private/allocate")

	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
	public Mono<Void> allocate(StoreMerchantId merchantStore, Domain domain) {
		routingService.addDomain(merchantStore, domain);
		return Mono.empty();
	}

	@DeleteMapping("private/remove")

	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
	public Mono<Void> remove(StoreMerchantId merchantStore, Domain domain) {
		routingService.removeDomain(merchantStore, domain);
		return Mono.empty();
	}

}
