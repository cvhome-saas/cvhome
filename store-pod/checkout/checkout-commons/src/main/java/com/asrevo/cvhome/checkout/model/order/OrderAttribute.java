package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class OrderAttribute implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String key;

    private String value;

}
