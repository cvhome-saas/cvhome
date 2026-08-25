package com.asrevo.cvhome.payment.model.payment;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.ReadableList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReadableTransactionList extends ReadableList<ReadableTransaction> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
