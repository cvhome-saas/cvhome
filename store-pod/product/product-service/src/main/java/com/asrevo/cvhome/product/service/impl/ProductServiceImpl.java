package com.asrevo.cvhome.product.service.impl;

import com.asrevo.cvhome.product.commons.domain.*;
import com.asrevo.cvhome.product.commons.dto.*;
import com.asrevo.cvhome.product.entity.ProductDetailsEntity;
import com.asrevo.cvhome.product.entity.ProductEntity;
import com.asrevo.cvhome.product.mappers.ProductMapper;
import com.asrevo.cvhome.product.repository.ProductDetailsRepository;
import com.asrevo.cvhome.product.repository.ProductRepository;
import com.asrevo.cvhome.product.service.CategoryService;
import com.asrevo.cvhome.product.service.ProductService;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
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
        validateSubProducts(storeId, createProductDto.productType(), createProductDto.subProducts());
        CategoryDto category = categoryService.findCategory(categoryId, storeId);
        ProductEntity savedProduct = productRepository.save(ProductEntity.createProduct(storeId, category.id(), createProductDto));
        return productMapper.toCreateProductResponseDto(savedProduct);
    }

    private void validateSubProducts(StoreId storeId, ProductType productType, SubProducts productIds) {
        if (ProductType.SINGLE.equals(productType)) {
            if (!CollectionUtils.isEmpty(productIds.productIds())) {
                throw new RuntimeException("single product should not have sub product");
            }
        } else {
            if (!CollectionUtils.isEmpty(productIds.productIds())) {
                List<ProductEntity> subProducts = productRepository.findAllById(productIds.productIds());
                if (!subProducts.stream().allMatch(it -> it.getStoreId().equals(storeId))) {
                    throw new RuntimeException("one or more sub product not from this store");
                }
                if (!subProducts.stream().allMatch(it -> ProductType.SINGLE.equals(it.getProductType()))) {
                    throw new RuntimeException("all sub products should be single type not GROUP OR VARIANT");
                }
            }
        }
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
        ProductEntity productEntity = productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId)
                .orElseThrow(() -> new RuntimeException("product not exist"));
        ProductDetailsEntity productDetailsEntity = productDetailsRepository.findByStoreIdAndAndProduct(storeId, productId)
                .orElseThrow(() -> new RuntimeException("product details not created yet"));
        List<ProductDetails> productDetails = getSubProductDetails(storeId, productEntity);
        productEntity.publish(productDetailsEntity.getProductDetails(), productDetails);
        productRepository.save(productEntity);
        return new PublishProductResponseDto(productId, Boolean.TRUE);
    }

    private List<ProductDetails> getSubProductDetails(StoreId storeId, ProductEntity productEntity) {
        List<ProductDetails> productDetails;
        if (ProductType.GROUP.equals(productEntity.getProductType()) || ProductType.VARIANT.equals(productEntity.getProductType())) {
            productDetails = productDetailsRepository.findAllByStoreIdAndAndProductIn(storeId, productEntity.getSubProducts().productIds())
                    .stream()
                    .map(ProductDetailsEntity::getProductDetails)
                    .toList();
            if (productDetails.size() != productEntity.getSubProducts().size()) {
                throw new RuntimeException("one of your sub products not have product details yet");
            }
        } else {
            productDetails = List.of();
        }
        return productDetails;
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

    @Transactional
    @Override
    public UpdateProductResponseDto updateProduct(StoreId storeId, ProductId productId, CategoryId categoryId, UpdateProductDto updateProductDto) {
        validateSubProducts(storeId, updateProductDto.productType(), updateProductDto.subProducts());
        CategoryDto category = categoryService.findCategory(categoryId, storeId);
        ProductEntity productEntity = productRepository.findOneByStoreIdAndIdAndDeletedIsFalse(storeId, productId).orElseThrow(() -> new RuntimeException("product not exist"));
        if (Boolean.TRUE.equals(productEntity.getPublished())) {
            ProductDetailsEntity productDetailsEntity = productDetailsRepository.findByStoreIdAndAndProduct(storeId, productId)
                    .orElseThrow(() -> new RuntimeException("update published product that have details not created yet"));
            List<ProductDetails> productDetails = getSubProductDetails(storeId, productEntity);
            productEntity.verify(productDetailsEntity.getProductDetails(), productDetails);
        }
        productMapper.map(updateProductDto, productEntity);
        productEntity.setCategory(AggregateReference.to(category.id()));
        ProductEntity save = productRepository.save(productEntity);
        return productMapper.toUpdateProductResponseDto(save);
    }
}
