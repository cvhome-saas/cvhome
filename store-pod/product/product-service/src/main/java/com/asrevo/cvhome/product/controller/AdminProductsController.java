package com.asrevo.cvhome.product.controller;

import com.asrevo.cvhome.product.service.AdminProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/products")
@AllArgsConstructor
@Slf4j
public class AdminProductsController {
    private final AdminProductService productService;

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

