package com.asrevo.cvhome.content.model.storefront;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.content.model.PolicyType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StorefrontPolicy implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private PolicyType type;

    private String slug;

    private int version;

    private String servedLocale;

    private String heading;

    private String body;

    private Instant effectiveFrom;

    private boolean requiresAcceptance;

}
