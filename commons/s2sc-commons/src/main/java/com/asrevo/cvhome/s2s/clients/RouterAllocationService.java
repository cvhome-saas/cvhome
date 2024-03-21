package com.asrevo.cvhome.s2s.clients;


import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.dto.PodReferenceDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("api/v1/router")
public interface RouterAllocationService {

    @GetExchange("allocation")
    PodReferenceDto getAllocation(@RequestParam Domain domain);
}