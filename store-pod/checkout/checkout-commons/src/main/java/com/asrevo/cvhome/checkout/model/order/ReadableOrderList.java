package com.asrevo.cvhome.checkout.model.order;

import java.io.Serial;

import com.asrevo.cvhome.commons.domain.ReadableList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableOrderList extends ReadableList<ReadableOrder> {

    @Serial
    private static final long serialVersionUID = 1L;
}
