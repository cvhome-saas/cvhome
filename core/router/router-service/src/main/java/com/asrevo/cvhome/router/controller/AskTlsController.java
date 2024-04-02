package com.asrevo.cvhome.router.controller;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.router.repository.ReferenceAlisRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class AskTlsController {
    public final ReferenceAlisRepository referenceAlisRepository;

    @GetMapping("public/ask-for-tls")
    public ResponseEntity<Object> ask(Domain domain) {
        return referenceAlisRepository.findByAlis(domain)
                .map(it -> ResponseEntity.ok().build())
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
