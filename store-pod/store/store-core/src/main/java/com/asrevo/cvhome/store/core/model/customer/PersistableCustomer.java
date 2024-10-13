package com.asrevo.cvhome.store.core.model.customer;

import com.asrevo.cvhome.store.core.model.customer.attribute.PersistableCustomerAttribute;
import com.asrevo.cvhome.store.core.model.security.PersistableGroup;
import java.io.Serial;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersistableCustomer extends CustomerEntity {

    @Serial private static final long serialVersionUID = 1L;

    /**
     *
     */
    private String password = null;

    private String repeatPassword = null;
    private List<PersistableCustomerAttribute> attributes;
    private List<PersistableGroup> groups;
}
