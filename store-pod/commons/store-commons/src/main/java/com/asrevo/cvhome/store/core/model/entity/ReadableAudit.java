package com.asrevo.cvhome.store.core.model.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableAudit implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Instant created;

    private Instant modified;

    private String user;

}
