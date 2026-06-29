package com.asrevo.cvhome.merchant.api;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1")
public interface MerchantStorePodClient {

    @PostExchange("/private/store")
    Void create(@RequestBody Map<Object, Object> dto);

    @GetExchange("private/store")
    Map<String, Object> getStore(@RequestParam("store") String store);

}
