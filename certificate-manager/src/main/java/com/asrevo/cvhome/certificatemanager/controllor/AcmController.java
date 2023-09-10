package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
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
    private final DomainCertificateOrderService domainCertificateOrderService;

    /*
     * .appcalc.net appcalc.net .uxplore.net uxplore.net www.uxplore.net uxb.uxplore.net
     * backend.uxplore.net
     */
    @PostMapping("order")
    public DomainCertificateOrder order(@RequestBody @Validated DomainCertificateOrder domainOrder) throws AcmeException, IOException {
        log.info("will order a certificate for domain {}", domainOrder.getDomain());
        return acmCertificateOrderService.initiateOrder(domainOrder);
    }

    @PostMapping("ask-validate")
    public void askValidate(@RequestBody DomainCertificateOrder certificateOrder, @RequestParam(value = "type", defaultValue = "Dns01") String type) throws AcmeException, IOException {
        log.info("will validate a domain challenge for {}", certificateOrder.getLocation());
        acmCertificateOrderService.askValidate(certificateOrder, type);
    }

    @PostMapping("domain-certificate-file")
    public ResponseEntity<InputStreamResource> getDomainCertificateFile(@RequestParam("domain") String domain, @RequestParam(name = "fileType", defaultValue = "CRT") CertificateFileType fileType) {
        log.info("will download certificate {} for domain {}", fileType.getType(), domain);
        InputStreamResource body = acmeManagerService.getDomainCertificateFile(domain, fileType);
        if (body != null) {
            return ResponseEntity.ok().headers(headers ->
                    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileType.getFile())).body(body);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("orders")
    public List<DomainCertificateOrder> order(@RequestBody List<Long> orderIds) {
        return domainCertificateOrderService.findAllOrderByIdIn(orderIds);
    }
}
