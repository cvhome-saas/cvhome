package com.asrevo.cvhome.domain.service;

import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.DomainCertificate;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@HttpExchange("api/v1/acm")
public interface AcmService {

    @PostExchange("order")
    Mono<ResponseEntity<DomainCertificateOrder>> order(@RequestBody DomainCertificateOrder domainOrder);

    @PostExchange("validate")
    Mono<ResponseEntity<DomainCertificateOrder>> validate(@RequestBody DomainCertificateOrder certificateOrder, @RequestParam(value = "type", defaultValue = "dns-01") String type);

    @PostExchange("generate")
    Mono<ResponseEntity<DomainCertificate>> generate(@RequestBody DomainCertificateOrder certificateOrder);

    @PostExchange("info")
    Mono<ResponseEntity<DomainCertificate>> info(@RequestBody DomainCertificateOrder certificateOrder);

    @PostExchange("domain-certificate-file")
    Mono<ResponseEntity<InputStreamResource>> getDomainCertificateFile(@RequestParam("domain") String domain, @RequestParam(name = "fileType", defaultValue = "CRT") CertificateFileType fileType);

    @PostExchange("orders")
    Mono<ResponseEntity<List<DomainCertificateOrder>>> findOrders(@RequestBody List<Long> externalAcmOrderIds);
}
