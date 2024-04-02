package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ShoppingCartAttribute extends ShopEntity implements Serializable {


    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private long optionId;
    private long optionValueId;
    private long attributeId;
    private String optionName;
    private String optionValue;

}
