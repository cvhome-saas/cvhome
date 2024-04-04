package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class IntegrationModuleEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String code;
    private boolean active;

}
