package com.asrevo.cvhome.store.core.model.shoppingcart;

import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShoppingCartAttribute extends ShopEntity implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private long optionId;
    private long optionValueId;
    private long attributeId;
    private String optionName;
    private String optionValue;
}
