package com.asrevo.cvhome.commons.domain;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ReadableList implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int totalPages; // totalPages
    private int number; // number of record in current page
    private long recordsTotal; // total number of records in db
    private int recordsFiltered;
}
