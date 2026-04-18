package com.asrevo.cvhome.store.core.model.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableAudit {

    private String created;

    private String modified;

    private String user;

}
