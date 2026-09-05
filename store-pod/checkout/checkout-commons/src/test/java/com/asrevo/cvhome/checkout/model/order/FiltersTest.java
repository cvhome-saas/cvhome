package com.asrevo.cvhome.checkout.model.order;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;

import static org.assertj.core.api.Assertions.assertThat;

class FiltersTest {

    @Test
    void theEmptyFiltersHaveNoCriteria() {
        assertThat(OrderFilter.none()).usingRecursiveComparison().isEqualTo(new OrderFilter(null, null, null, null, null, null));
        assertThat(CustomerFilter.none()).usingRecursiveComparison().isEqualTo(new CustomerFilter(null, null, null, null, null));
    }
}
