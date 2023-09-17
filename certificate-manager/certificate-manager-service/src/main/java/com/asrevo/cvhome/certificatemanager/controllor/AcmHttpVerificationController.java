package com.asrevo.cvhome.certificatemanager.controllor;

import com.asrevo.cvhome.certificatemanager.domain.HttpValidationToken;
import com.asrevo.cvhome.certificatemanager.service.AcmeManagerService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.InputStreamReader;

@RestController
@RequestMapping(".well-known/acme-challenge")
@AllArgsConstructor
public class AcmHttpVerificationController {
    private AcmeManagerService acmeManagerService;

    @GetMapping("{token}")
    ResponseEntity<InputStreamReader> verify(@PathVariable("token") String token) {
        HttpValidationToken validationToken = new HttpValidationToken(token);
        InputStream stream = acmeManagerService.getHttpValidationFile(validationToken);
        InputStreamReader body = new InputStreamReader(stream);
        return ResponseEntity.ok().headers(headers ->
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + token)).body(body);
    }
}
