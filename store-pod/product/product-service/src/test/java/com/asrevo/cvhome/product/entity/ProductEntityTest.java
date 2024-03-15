package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.product.commons.domain.ProductDetails;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
@Tag("unit-test")
class ProductEntityTest {

    @Test
    void publish() {
        ProductEntity entity = new ProductEntity();
        ProductDetails pd = new ProductDetails(Map.of(), List.of());
        entity.publish(pd);
    }
}