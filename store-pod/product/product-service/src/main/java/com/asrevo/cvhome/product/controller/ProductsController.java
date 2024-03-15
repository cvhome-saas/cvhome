package com.asrevo.cvhome.product.controller;

import com.asrevo.cvhome.product.commons.dto.ProductDto;
import com.asrevo.cvhome.product.service.AdminProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/public/products")
@AllArgsConstructor
@Slf4j
public class ProductsController {
    private final AdminProductService productService;

    @GetMapping("find-all")
    public List<ProductDto> findAll(@RequestParam StoreId storeId, Pageable request) {
        return productService.findAll(storeId, request);
    }
}

