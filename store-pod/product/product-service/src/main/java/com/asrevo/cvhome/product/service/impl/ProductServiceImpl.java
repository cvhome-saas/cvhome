package com.asrevo.cvhome.product.service.impl;

import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.entity.ProductEntity;
import com.asrevo.cvhome.product.mappers.ProductMapper;
import com.asrevo.cvhome.product.repository.ProductRepository;
import com.asrevo.cvhome.product.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductDto> findAll(StoreId storeId, Pageable pageable) {
        return productRepository.findAllByStoreId(storeId, pageable).stream().map(productMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CreateProductResponseDto createProduct(StoreId storeId, CreateProductDto createProductDto) {
        ProductEntity entity = productRepository.save(ProductEntity.createProduct(storeId, createProductDto));
        return productMapper.toCreateProductResponseDto(entity);
    }

    @Transactional
    @Override
    public UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, UpdateProductDto updateProductDto) {
        return productRepository.findOneByStoreIdAndId(storeId, productId)
                .map(it -> {
                    productMapper.map(updateProductDto, it);
                    return it;
                })
                .map(it -> productMapper.toUpdateProductResponseDto(productRepository.save(it)))
                .orElse(null);
    }
}
