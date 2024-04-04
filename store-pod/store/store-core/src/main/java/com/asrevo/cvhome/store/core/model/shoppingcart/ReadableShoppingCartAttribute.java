package com.asrevo.cvhome.store.core.model.shoppingcart;


import com.asrevo.cvhome.store.core.model.entity.ShopEntity;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableShoppingCartAttribute extends ShopEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ReadableShoppingCartAttributeOption option;
    private ReadableShoppingCartAttributeOptionValue optionValue;

}
