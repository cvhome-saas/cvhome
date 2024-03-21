package com.asrevo.cvhome.router.controller;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainReference;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import com.asrevo.cvhome.router.commons.dto.AddAlisDto;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.router.service.PodService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/router")
@AllArgsConstructor
@Slf4j
public class RouterController {
    private final PodService podService;

    @PostMapping("create")
    public CreateReferenceResponse createReference(@RequestBody CreateNewReferenceDto dto) {
        return podService.createReference(dto);
    }

    @PostMapping("add-alis")
    public CreateReferenceResponse addAlis(@RequestBody AddAlisDto dto) {
        return podService.addAlis(dto);
    }

    @GetMapping("allocation")
    public PodReferenceDto getAllocation(@RequestParam Domain domain) {
        return podService.getAllocation(domain);
    }

    @PostMapping("enable-reference")
    public ResponseEntity<Void> enableReference(@RequestBody DomainReference reference) {
        podService.enableReference(reference);
        return ResponseEntity.ok().build();
    }

    @PostMapping("disable-reference")
    public ResponseEntity<Void> disableReference(@RequestBody DomainReference reference) {
        podService.disableReference(reference);
        return ResponseEntity.ok().build();
    }

}
