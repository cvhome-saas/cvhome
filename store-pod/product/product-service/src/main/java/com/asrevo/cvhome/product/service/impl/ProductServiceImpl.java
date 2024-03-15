package com.asrevo.cvhome.product.service.impl;

import com.asrevo.cvhome.product.commons.domain.*;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.entity.ProductDetailsEntity;
import com.asrevo.cvhome.product.entity.ProductEntity;
import com.asrevo.cvhome.product.entity.ProductImageEntity;
import com.asrevo.cvhome.product.entity.ProductVariantEntity;
import com.asrevo.cvhome.product.mappers.ProductMapper;
import com.asrevo.cvhome.product.repository.ProductDetailsRepository;
import com.asrevo.cvhome.product.repository.ProductImageRepository;
import com.asrevo.cvhome.product.repository.ProductRepository;
import com.asrevo.cvhome.product.repository.ProductVariantRepository;
import com.asrevo.cvhome.product.service.CategoryService;
import com.asrevo.cvhome.product.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryService categoryService;
    private final ProductDetailsRepository productDetailsRepository;
    private final ProductMapper productMapper;

    @Override
    public List<ProductDto> findAll(StoreId storeId, Pageable pageable) {
        return productRepository.findAllByStoreId(storeId, pageable).stream().map(productMapper::toDto).toList();
    }

    @Transactional
    @Override
    public CreateProductResponseDto createProduct(StoreId storeId, CategoryId categoryId, CreateProductDto createProductDto) {
        CategoryDto category = categoryService.findCategory(categoryId, storeId);
        ProductEntity savedProduct = productRepository.save(ProductEntity.createProduct(storeId, category.id(), createProductDto));
        return productMapper.toCreateProductResponseDto(savedProduct);
    }

    @Transactional
    @Override
    public DeleteProductResponseDto deleteProduct(StoreId storeId, ProductId productId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .map(ProductEntity::delete)
                .map(productRepository::save)
                .map(it -> new DeleteProductResponseDto(it.getId(), Boolean.TRUE))
                .orElse(null);
    }

    @Transactional
    @Override
    public ProductDetails addProductDetails(StoreId storeId, ProductId productId, ProductDetails productDetails) {
        return productDetailsRepository.save(ProductDetailsEntity.create(storeId, productId, productDetails)).getProductDetails();
    }

    @Transactional
    @Override
    public PublishProductResponseDto publishProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId).orElseThrow(() -> new RuntimeException("product not exist"));
        ProductDetailsEntity productDetailsEntity = productDetailsRepository.findByStoreIdAndAndProduct(storeId, productId).orElseThrow(() -> new RuntimeException("product details not created yet"));
        productEntity.publish(productDetailsEntity.getProductDetails());
        productRepository.save(productEntity);
        return new PublishProductResponseDto(productId, Boolean.TRUE);
    }

    @Transactional
    @Override
    public PublishProductResponseDto unPublishProduct(StoreId storeId, ProductId productId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .map(ProductEntity::unPublish)
                .map(productRepository::save)
                .map(it -> new PublishProductResponseDto(it.getId(), Boolean.FALSE))
                .orElse(null);
    }

    @Override
    public ProductDto getProduct(StoreId storeId, ProductId productId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .map(productMapper::toDto)
                .orElse(null);
    }

    @Override
    public DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId) {
        ProductEntity productEntity = productRepository.findOneByStoreIdAndIdAndDeletedIsFalseAndPublishedIsTrue(storeId, productId)
                .orElseThrow(() -> new RuntimeException("product not exist or not published"));
        return new DetailedProductDto();
    }

    @Override
    public DetailedProductDto getDetailedProduct(StoreId storeId, ProductId productId, ProductVariantId variantId) {
        ProductEntity productEntity = productRepository.findOneByStoreIdAndIdAndDeletedIsFalseAndPublishedIsTrue(storeId, productId)
                .orElseThrow(() -> new RuntimeException("product not exist or not published"));
        return new DetailedProductDto();
    }

    @Transactional
    @Override
    public UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, CategoryId categoryId, UpdateProductDto updateProductDto) {
        CategoryDto category = categoryService.findCategory(categoryId, storeId);
        ProductEntity currentProduct = productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId).orElseThrow(() -> new RuntimeException("product not exist"));
        productMapper.map(updateProductDto, currentProduct);
        currentProduct.setCategory(AggregateReference.to(category.id()));
        ProductEntity save = productRepository.save(currentProduct);
        return productMapper.toUpdateProductResponseDto(save);
    }

    @Transactional
    @Override
    public AddProductImageResponseDto addImage(StoreId storeId, ProductId productId, ImageLink imageLink) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .map(it -> ProductImageEntity.create(storeId, it.getId(), imageLink))
                .map(productImageRepository::save)
                .map(productMapper::toDto)
                .orElse(null);
    }

    @Transactional
    @Override
    public AddProductVariantResponseDto addVariant(StoreId storeId, ProductId productId, AddProductVariantDto addProductVariantDto) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .map(it -> ProductVariantEntity.createProductVariant(it.getId(), storeId, addProductVariantDto))
                .map(productVariantRepository::save)
                .map(productMapper::toAddProductVariantResponseDto)
                .orElse(null);

    }

    @Override
    public ProductVariantDto getProductVariant(StoreId storeId, ProductId productId, ProductVariantId variantId) {
        return productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .flatMap(it -> productVariantRepository.findById(variantId))
                .map(productMapper::toDto)
                .orElse(null);
    }
}
