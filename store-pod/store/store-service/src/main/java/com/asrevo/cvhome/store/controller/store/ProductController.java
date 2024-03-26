package com.asrevo.cvhome.store.controller.store;

import com.asrevo.cvhome.store.service.ProductService;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/public/product")
@AllArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping("detailed-product")
    public DetailedProductDto getDetailedProduct(@RequestParam StoreId storeId, @RequestParam ProductId productId) {
        return productService.getDetailedProduct(storeId, productId);
    }

    @PostMapping("find-all")
    public Page<ProductDto> findAll(@RequestParam StoreId storeId, Pageable pageable) {
        return productService.findAll(storeId, pageable);
    }
}

