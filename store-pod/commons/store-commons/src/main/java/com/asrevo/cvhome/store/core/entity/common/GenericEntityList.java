package com.asrevo.cvhome.store.core.entity.common;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GenericEntityList<T extends Serializable> extends EntityList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list;

}
