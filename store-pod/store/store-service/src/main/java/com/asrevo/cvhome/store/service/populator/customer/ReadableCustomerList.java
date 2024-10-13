package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.commons.domain.ReadableList;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomer;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerList extends ReadableList {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableCustomer> customers = new ArrayList<>();
}
