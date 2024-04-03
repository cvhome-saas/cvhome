package com.asrevo.cvhome.store.service.populator.customer;


import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import com.asrevo.cvhome.store.core.model.entity.ReadableList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableCustomerList extends ReadableList {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<ReadableCustomer> customers = new ArrayList<>();

}
