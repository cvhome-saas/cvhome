package com.asrevo.cvhome.dcm.controller;

import com.asrevo.cvhome.dcm.domain.HttpValidationToken;
import com.asrevo.cvhome.dcm.service.AcmCertificateOrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.InputStreamReader;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

@RestController
@RequestMapping(".well-known/acme-challenge")
@AllArgsConstructor
public class AcmHttpVerificationController {
    private AcmCertificateOrderService acmCertificateOrderService;

    @GetMapping("{token}")
    ResponseEntity<InputStreamReader> verify(@PathVariable("token") String token) {
        HttpValidationToken validationToken = new HttpValidationToken(token);
        InputStream stream = acmCertificateOrderService.getHttpValidationToken(validationToken);
        if (stream != null) {
            return ResponseEntity.ok()
                    .headers(headers ->
                            headers.add(CONTENT_DISPOSITION, "attachment;filename=" + token))
                    .body(new InputStreamReader(stream));
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}
