package com.asrevo.cvhome.store.core.model.catalog.product;

import com.asrevo.cvhome.store.core.model.catalog.NamedEntity;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductPriceDescription extends NamedEntity {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private String priceAppender;
}
