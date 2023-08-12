package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.commons.domain.CertificateFileType;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificate;
import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
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

@RestController
@RequestMapping("api/acm")
@AllArgsConstructor
@Slf4j
public class AcmController {

    private final AcmeManagerService acmeManagerService;
    private final DomainCertificateOrderService domainCertificateOrderService;

    /*
     * .appcalc.net appcalc.net .uxplore.net uxplore.net www.uxplore.net uxb.uxplore.net
     * backend.uxplore.net
     */
    @PostMapping("order")
    public DomainCertificateOrder order(@RequestBody @Validated DomainCertificateOrder domainOrder) throws AcmeException, IOException {
        return domainCertificateOrderService.order(domainOrder);
    }

    @PostMapping("validate")
    public DomainCertificateOrder validate(@RequestBody DomainCertificateOrder certificateOrder, @RequestParam(value = "type", defaultValue = "dns-01") String type) throws AcmeException, IOException {
        return domainCertificateOrderService.validate(certificateOrder, type);
    }

    @PostMapping("generate")
    public DomainCertificate generate(@RequestBody DomainCertificateOrder certificateOrder) throws AcmeException, IOException {
        return domainCertificateOrderService.generate(certificateOrder);
    }

    @PostMapping("info")
    public DomainCertificate info(@RequestBody DomainCertificateOrder certificateOrder) throws MalformedURLException {
        return acmeManagerService.info(certificateOrder);
    }

    @PostMapping("domain-certificate-file")
    public ResponseEntity<InputStreamResource> getDomainCertificateFile(@RequestParam("domain") String domain, @RequestParam(name = "fileType", defaultValue = "CRT") CertificateFileType fileType) {
        InputStreamResource body = acmeManagerService.getDomainCertificateFile(domain, fileType);
        return ResponseEntity.ok().headers(httpHeaders -> httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + fileType.getFile())).body(body);
    }
}
