package com.asrevo.cvhome.s2s.clients;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("api/v1/router")
public interface RouterAllocationService {
    @GetExchange("allocation-by-domain")
    Mono<ManagerStoreId> getAllocation(Domain domain);
}
