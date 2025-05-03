package com.asrevo.cvhome.store.core.model.entity;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Deprecated
public class ReadableEntityList<T> extends ReadableList<T> {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<T> content;
}
