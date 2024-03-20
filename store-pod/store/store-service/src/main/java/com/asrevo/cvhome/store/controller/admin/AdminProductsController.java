package com.asrevo.cvhome.store.controller.admin;

import com.asrevo.cvhome.storepod.commons.domain.StoreId;
import com.asrevo.cvhome.storepod.commons.domain.ProductId;
import com.asrevo.cvhome.store.commons.dto.*;
import com.asrevo.cvhome.store.service.AdminProductService;
import com.asrevo.cvhome.storepod.commons.dto.DetailedProductDto;
import com.asrevo.cvhome.storepod.commons.dto.FindAllProductDto;
import com.asrevo.cvhome.storepod.commons.dto.ProductDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/admin/products")
@AllArgsConstructor
@Slf4j
public class AdminProductsController {
    private final AdminProductService productService;

    @PostMapping("create")
    public DetailedProductDto createProduct(@RequestParam StoreId storeId, @RequestBody CreateDetailedProductDto detailedProductDto) {
        return productService.createProduct(storeId, detailedProductDto);
    }

    @GetMapping("detailed-product")
    public DetailedProductDto getDetailedProduct(@RequestParam StoreId storeId, @RequestParam ProductId productId) {
        return productService.getDetailedProduct(storeId, productId);
    }

    @PostMapping("find-all")
    public Page<ProductDto> findAll(@RequestParam StoreId storeId, Pageable pageable, @RequestBody FindAllProductDto findAllProductDto) {
        return productService.findAll(storeId, findAllProductDto, pageable);
    }

    @DeleteMapping("delete")
    public DeleteProductResponseDto delete(@RequestParam StoreId storeId, @RequestParam ProductId productId) {
        return productService.deleteProduct(storeId, productId);
    }

    @PostMapping("publish")
    public PublishProductResponseDto publish(@RequestParam StoreId storeId, @RequestParam ProductId productId) {
        return productService.publishProduct(storeId, productId);
    }

    @PostMapping("un-publish")
    public PublishProductResponseDto unPublish(@RequestParam StoreId storeId, @RequestParam ProductId productId) {
        return productService.unPublishProduct(storeId, productId);
    }

}

