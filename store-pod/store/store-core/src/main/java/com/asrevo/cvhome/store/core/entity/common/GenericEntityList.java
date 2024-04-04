package com.asrevo.cvhome.store.core.entity.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class GenericEntityList<T> extends EntityList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<T> list;

}
