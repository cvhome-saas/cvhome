package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** A deliberately anonymous storefront read on a pod service: no gate, no {@code /private/}. */
@RestController
@RequestMapping("/api/v1")
public class StorefrontApi {

    @GetMapping("/cart/{code}")
    public String cart() {
        return "cart";
    }

}
