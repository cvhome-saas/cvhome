package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A store-core controller: one anonymous read under {@code /public/}, one that any signed-in principal may call. */
@RestController
@RequestMapping("/api/v1/plans")
public class PublicApi {

    @GetMapping("/public/catalog")
    public String catalog() {
        return "catalog";
    }

    @GetMapping("/me")
    public String me() {
        return "me";
    }

}
