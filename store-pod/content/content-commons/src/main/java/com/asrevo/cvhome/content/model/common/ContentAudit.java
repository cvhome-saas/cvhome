package com.asrevo.cvhome.content.model.common;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant createdAt;

    private String createdBy;

    private Instant updatedAt;

    private String updatedBy;

}
