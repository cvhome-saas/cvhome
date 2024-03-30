package com.asrevo.cvhome.store.core.model.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ReadableEntityList<T> extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<T> items;

}
