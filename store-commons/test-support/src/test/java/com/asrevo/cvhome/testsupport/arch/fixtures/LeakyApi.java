package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The bug the rule exists for: a {@code /private/} handler that forgot its token. */
@RestController
@RequestMapping("/api/v1")
public class LeakyApi {

    @GetMapping("/private/customers")
    public String customers() {
        return "customers";
    }

}
