package com.asrevo.cvhome.store.core.model.entity;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.ReadableList;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableEntityList<T> extends ReadableList<T> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

}
