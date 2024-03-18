package com.asrevo.cvhome.landing.service;


import com.asrevo.cvhome.commons.utils.CustomPageImpl;
import com.asrevo.cvhome.manager.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("api/v1/public/product")
public interface ProductService {
    @GetExchange("detailed-product")
    DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId);

    @PostExchange("find-all")
    CustomPageImpl<ProductDto> findAll(StoreId storeId, PageRequest pageable);
}
