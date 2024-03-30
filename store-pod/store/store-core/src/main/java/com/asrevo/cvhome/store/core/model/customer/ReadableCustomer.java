package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.customer.attribute.ReadableCustomerAttribute;
import com.asrevo.cvhome.store.core.model.security.ReadableGroup;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Setter
@Getter
public class ReadableCustomer extends CustomerEntity implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ReadableCustomerAttribute> attributes = new ArrayList<ReadableCustomerAttribute>();
    private List<ReadableGroup> groups = new ArrayList<ReadableGroup>();

}
