package com.asrevo.cvhome.store.core.model.entity;

import com.asrevo.cvhome.commons.domain.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.List;

@Setter
@Getter
public class ReadableEntityList<T> extends ReadableList {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<T> items;

}
