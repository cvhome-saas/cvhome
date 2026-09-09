package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.web.bind.annotation.GetMapping;

/** A mapped method on a class that is not a controller is not a handler and must not be selected. */
public class NotAController {

    @GetMapping("/ignored")
    public String ignored() {
        return "ignored";
    }

}
