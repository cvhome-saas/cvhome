package com.asrevo.cvhome.gateway.service;

import com.asrevo.cvhome.commons.domain.CertificateFileType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;


@HttpExchange("api/acm")
public interface AcmService {
    @PostExchange("/domain-certificate-file")
    Mono<ResponseEntity<byte[]>> getDomainCertificateFile(@RequestParam(name = "domain") String domain, @RequestParam CertificateFileType fileType);
}
