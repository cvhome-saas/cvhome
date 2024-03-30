package com.asrevo.cvhome.store.core.entity.customer;

import com.asrevo.cvhome.store.core.entity.common.EntityList;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class CustomerList extends EntityList {


    /**
     *
     */
    private static final long serialVersionUID = -3108842276158069739L;
    private List<Customer> customers = new ArrayList<>();

}
