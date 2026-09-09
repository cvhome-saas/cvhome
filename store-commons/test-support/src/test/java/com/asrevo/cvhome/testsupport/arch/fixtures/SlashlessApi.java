package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Mappings with no leading slash on either side — tenancy's {@code SignUpApi} shape. Spring joins them with one;
 * a rule that concatenates would see {@code api/v1/signuppublic/create} and lose the segment it keys on.
 */
@RestController
@RequestMapping("api/v1/signup")
public class SlashlessApi {

    @PostMapping("public/create")
    public String create() {
        return "created";
    }

    @GetMapping("private/keys")
    public String keys() {
        return "keys";
    }

}
