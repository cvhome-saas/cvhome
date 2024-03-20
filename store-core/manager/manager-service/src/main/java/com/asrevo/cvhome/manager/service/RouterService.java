package com.asrevo.cvhome.manager.service;


import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.router.commons.dto.AddAlisDto;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import com.asrevo.cvhome.router.commons.dto.PodDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("api/v1/router")
public interface RouterService {

    @PostExchange("create")
    CreateReferenceResponse create(@RequestBody CreateNewReferenceDto dto);

    @PostExchange("add-alis")
    CreateReferenceResponse addAlis(@RequestBody AddAlisDto dto);

    @GetExchange("allocation")
    PodDto getAllocation(@RequestParam Domain domain);
}