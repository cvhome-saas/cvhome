package com.asrevo.cvhome.product.controller;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/products")
@AllArgsConstructor
@Slf4j
public class AdminProductsController {
    private final ProductService productService;

/*    @PostMapping
    public CreateProductResponseDto createProduct(@RequestParam StoreId storeId, @RequestBody CreateProductDto createProductDto) {
        return productService.createProduct(storeId, createProductDto);
    }

    @PutMapping
    public UpdateProductResponseDto updateProduct(@RequestParam StoreId storeId, @RequestParam ProductId productId, @RequestBody UpdateProductDto updateProductDto) {
        return productService.updateProduct(storeId, productId, updateProductDto);
    }

    @GetMapping("find-all")
    public List<ProductDto> findAll(@RequestParam StoreId storeId, Pageable pageable) {
        return productService.findAll(storeId, pageable);
    }*/
}

