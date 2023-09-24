package com.asrevo.cvhome.certificatemanager.controller;

import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.OrdersService;
import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.dto.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/acm")
@AllArgsConstructor
@Slf4j
public class AcmController {

    private final AcmeManagerService acmeManagerService;
    private final AcmCertificateOrderService acmCertificateOrderService;
    private final OrdersService ordersService;

    @PostMapping("register")
    public DomainCreateResponseDto register(@RequestBody @Validated DomainCreateRequestDto createRequest) {
        log.info("will register a domain {}", createRequest.getDomain());
        return acmCertificateOrderService.register(createRequest);
    }

    /*
     * .appcalc.net appcalc.net .uxplore.net uxplore.net www.uxplore.net uxb.uxplore.net
     * backend.uxplore.net
     */
    @PostMapping("order")
    public OrdersCreateResponseDto order(@RequestBody @Validated OrdersCreateRequestDto createRequest) {
        log.info("will order a certificate for domain {}", createRequest.getDomain());
        return acmCertificateOrderService.initiateOrder(createRequest);
    }

    @PostMapping("validate")
    public void validate(@RequestBody OrdersId orderId, @RequestParam(value = "type", defaultValue = "Dns01") String type) throws AcmeException, IOException {
        log.info("will validate a domain challenge for {}", orderId);
        acmCertificateOrderService.askValidate(orderId, type);
    }

    @PostMapping("domain-certificate-file")
    public ResponseEntity<InputStreamResource> getCertificateFile(Domain domain, @RequestParam(name = "fileType", defaultValue = "CRT") CertificateFileType fileType) {
        log.info("will download certificate {} for domain {}", fileType.getType(), domain.domain());
        InputStreamResource body = acmeManagerService.getCertificateFile(domain, fileType);
        if (body != null) {
            return ResponseEntity.ok().headers(headers ->
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileType.getFile())).body(body);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    // @TODO check still need it

    @PostMapping("orders")
    public List<OrdersResponseDto> order(@RequestBody List<OrdersId> ordersIds) {
        return ordersService.findAllOrderByIdIn(ordersIds);
    }
}
