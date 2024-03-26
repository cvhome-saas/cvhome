package com.asrevo.cvhome.manager.service;


import com.asrevo.cvhome.router.commons.dto.AddAlisDto;
import com.asrevo.cvhome.router.commons.dto.CreateNewReferenceDto;
import com.asrevo.cvhome.router.commons.dto.CreateReferenceResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("api/v1/router")
public interface RouterService {

    @PostExchange("create")
    CreateReferenceResponse create(@RequestBody CreateNewReferenceDto dto);

    @PostExchange("add-alis")
    CreateReferenceResponse addAlis(@RequestBody AddAlisDto dto);
}