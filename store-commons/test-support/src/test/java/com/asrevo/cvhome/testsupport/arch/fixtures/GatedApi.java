package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Every handler carries its token — the shape the rule wants to see. */
@RestController
@RequestMapping("/api/v1")
public class GatedApi {

    @GetMapping("/private/orders")
    @PreAuthorize("hasPermission(null, 'STORE-POD.CHECKOUT.*')")
    public String orders() {
        return "orders";
    }

    @PostMapping("/private/orders/{id}/history")
    @PreAuthorize("hasPermission(null, 'STORE-POD.CHECKOUT.*')")
    public String transition() {
        return "history";
    }

}
