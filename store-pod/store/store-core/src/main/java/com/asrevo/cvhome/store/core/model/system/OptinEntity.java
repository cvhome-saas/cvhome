package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class OptinEntity extends Optin {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Date startDate;
    private Date endDate;
    private String optinType;
    private String store;
    private String code;
    private String description;


}
