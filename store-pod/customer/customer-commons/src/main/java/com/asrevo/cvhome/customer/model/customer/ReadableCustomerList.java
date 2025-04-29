package com.asrevo.cvhome.customer.model.customer;

import com.asrevo.cvhome.commons.domain.ReadableList;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomerList extends ReadableList implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableCustomer> customers = new ArrayList<>();
}
