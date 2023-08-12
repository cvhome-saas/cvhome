package com.asrevo.cvhome.certificatemanager.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainRequest {

    private String domain;

    private URL location;

}
