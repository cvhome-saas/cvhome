package com.asrevo.cvhome.product.entity;

import com.asrevo.cvhome.product.commons.domain.*;
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
        ProductDetails pd = new ProductDetails(Map.of(DetailsLanguage.EN, new ProductDetail("sa", "wa", List.of(), Map.of(), Boolean.FALSE)), new ImagesLink(List.of(new ImageLink("https://google.com/421.png"))));
        entity.publish(pd);
    }
}