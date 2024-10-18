package com.asrevo.cvhome.store.core.model.system;

import java.io.Serial;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OptinEntity extends Optin {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private Date startDate;
    private Date endDate;
    private String optinType;
    private String store;
    private String code;
    private String description;
}
