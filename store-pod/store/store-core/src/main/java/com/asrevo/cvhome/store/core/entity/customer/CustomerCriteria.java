package com.asrevo.cvhome.store.core.entity.customer;

import com.asrevo.cvhome.store.core.entity.common.Criteria;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CustomerCriteria extends Criteria {

    private String firstName;
    private String lastName;
    private String name;
    private String email;
    private String country;
}
