package com.asrevo.cvhome.store.core.model.order.v1;

import com.asrevo.cvhome.commons.domain.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;


@Setter
@Getter
public class ReadableOrderList extends ReadableList implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;
    private List<ReadableOrder> orders;


}
