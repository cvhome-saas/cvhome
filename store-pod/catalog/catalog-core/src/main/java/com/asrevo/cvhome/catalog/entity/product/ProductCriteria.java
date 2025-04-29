package com.asrevo.cvhome.catalog.entity.product;

import com.asrevo.cvhome.catalog.entity.product.attribute.AttributeCriteria;
import com.asrevo.cvhome.store.core.entity.common.Criteria;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductCriteria extends Criteria {

    public static final String ORIGIN_SHOP = "shop";

    private String productName;
    private List<AttributeCriteria> attributeCriteria;
    private String origin = ORIGIN_SHOP;

    private Boolean available = null;

    private List<Long> categoryIds;
    private List<String> availabilities;
    private List<Long> productIds;
    private List<Long> optionValueIds;
    private String sku;

    // V2
    private List<String> optionValueCodes;
    private String option;

    private String status;

    private Long manufacturerId = null;

    private Long ownerId = null;
}
