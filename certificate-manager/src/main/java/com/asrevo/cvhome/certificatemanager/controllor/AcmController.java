package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.commons.domain.DomainCertificate;
import com.asrevo.cvhome.commons.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.certificatemanager.service.IDomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.CertificateFileType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("api/v1/acm")
@AllArgsConstructor
@Slf4j
public class AcmController {

    private final AcmeManagerService acmeManagerService;
    private final DomainCertificateOrderService domainCertificateOrderService;
    private final IDomainCertificateOrderService iDomainCertificateOrderService;

    /*
     * .appcalc.net appcalc.net .uxplore.net uxplore.net www.uxplore.net uxb.uxplore.net
     * backend.uxplore.net
     */
    @PostMapping("order")
    public DomainCertificateOrder order(@RequestBody @Validated DomainCertificateOrder domainOrder) throws AcmeException, IOException {
        log.info("will order a certificate for domain {}", domainOrder.getDomain());
        return domainCertificateOrderService.order(domainOrder);
    }

    @PostMapping("orders")
    public List<DomainCertificateOrder> order(@RequestBody List<Long> orderIds) {
        return iDomainCertificateOrderService.findAllOrderByIdIn(orderIds);
    }

    @PostMapping("validate")
    public DomainCertificateOrder validate(@RequestBody DomainCertificateOrder certificateOrder, @RequestParam(value = "type", defaultValue = "dns-01") String type) throws AcmeException, IOException {
        log.info("will validate a domain challenge for {}", certificateOrder.getLocation());
        return domainCertificateOrderService.validate(certificateOrder, type);
    }

    @PostMapping("generate")
    public DomainCertificate generate(@RequestBody DomainCertificateOrder certificateOrder) throws AcmeException, IOException {
        log.info("will generate a certification for {}", certificateOrder.getLocation());
        return domainCertificateOrderService.generate(certificateOrder);
    }

    @PostMapping("info")
    public DomainCertificate info(@RequestBody DomainCertificateOrder certificateOrder) throws MalformedURLException {
        log.info("will get info for {}", certificateOrder.getLocation());
        return acmeManagerService.info(certificateOrder);
    }

    @PostMapping("domain-certificate-file")
    public ResponseEntity<InputStreamResource> getDomainCertificateFile(@RequestParam("domain") String domain, @RequestParam(name = "fileType", defaultValue = "CRT") CertificateFileType fileType) {
        log.info("will download certificate {} for domain {}", fileType.getType(), domain);
        InputStreamResource body = acmeManagerService.getDomainCertificateFile(domain, fileType);
        if (body != null) {
            return ResponseEntity.ok().headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileType.getFile())).body(body);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
