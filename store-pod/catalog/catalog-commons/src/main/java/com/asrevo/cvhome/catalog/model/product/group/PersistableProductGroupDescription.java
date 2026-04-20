package com.asrevo.cvhome.catalog.model.product.group;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.store.core.model.catalog.NamedEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableProductGroupDescription extends NamedEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
