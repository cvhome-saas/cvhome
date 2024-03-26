package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.storepod.commons.dto.CreateStoreResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("api/v1/admin/store")
public interface StorePodClient {
    @PostExchange("create")
    CreateStoreResponse create(@RequestBody CreateStoreResponse dto);

}
