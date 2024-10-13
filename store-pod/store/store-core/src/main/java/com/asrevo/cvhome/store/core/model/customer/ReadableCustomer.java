package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.customer.attribute.ReadableCustomerAttribute;
import com.asrevo.cvhome.store.core.model.security.ReadableGroup;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableCustomer extends CustomerEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private List<ReadableCustomerAttribute> attributes = new ArrayList<>();
    private List<ReadableGroup> groups = new ArrayList<>();
}
