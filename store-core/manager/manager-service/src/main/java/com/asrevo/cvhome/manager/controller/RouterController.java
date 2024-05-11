package com.asrevo.cvhome.manager.controller;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.service.RouterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {
    public final RouterService routerService;

    @GetMapping("public/ask-for-tls")
    public ResponseEntity<Object> ask(Domain domain) {
        return Optional.ofNullable(routerService.getReferenceByDomain(domain))
                .map(it -> ResponseEntity.ok().build())
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("allocation-by-domain")
    public ManagerStoreId getAllocationByDomain(@RequestParam Domain domain) {
        return routerService.getReferenceByDomain(domain);
    }
}
