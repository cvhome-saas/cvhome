package com.asrevo.cvhome.commons.domain;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ReadableList<T extends Serializable> implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private int totalPages; // totalPages

    private int size; // number of record in current page

    private long totalElements; // total number of records in db

    private int recordsFiltered;

    private int pageNumber;

    private List<T> content;

}
