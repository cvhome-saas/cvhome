package com.asrevo.cvhome.checkout.services.order;

import com.asrevo.cvhome.commons.domain.StatisticList;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The console dashboard's three charts, over this store's orders only.
 */
public interface OrderStatisticsService {

    /** Orders per day, split by status. */
    StatisticList orders(StoreMerchantId store, StatisticRange range);

    /** Distinct customers per billing country. */
    StatisticList customers(StoreMerchantId store, StatisticRange range);

    /** Units sold per sku. */
    StatisticList products(StoreMerchantId store, StatisticRange range);
}
