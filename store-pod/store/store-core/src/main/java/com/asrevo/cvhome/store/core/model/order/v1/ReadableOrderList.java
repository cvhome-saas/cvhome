package com.asrevo.cvhome.store.core.model.order.v1;

import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;


@Setter
@Getter
public class ReadableOrderList extends ReadableList implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ReadableOrder> orders;


}
