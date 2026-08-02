package com.asrevo.cvhome.catalog.service.facade.product.group;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupListV2;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableMinimalProductPopulator;
import com.asrevo.cvhome.catalog.service.populator.catalog.product.PersistableProductGroupPopulator;
import com.asrevo.cvhome.catalog.service.populator.catalog.product.ReadableProductGroupPopulator;
import com.asrevo.cvhome.catalog.services.pricing.PricingService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.group.ProductGroupService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.ImageFilePath;

import lombok.extern.slf4j.Slf4j;

@Service("productGroupFacade")
@Slf4j
public class ProductGroupFacadeImpl implements ProductGroupFacade {

    private static final String PRODUCT_GROUP_NOT_FOUND_FOR_CODE_TEMPLATE = "ProductGroup not found for code: %s";

    private final ProductGroupService productGroupService;

    private final ProductService productService;

    private final ReadableProductGroupPopulator readableProductGroupPopulator;

    private final PersistableProductGroupPopulator persistableProductGroupPopulator;

    public ProductGroupFacadeImpl(ProductGroupService productGroupService, ProductService productService,
                                  PersistableProductGroupPopulator persistableProductGroupPopulator, PricingService pricingService,
                                  ImageFilePath imageUtils) {
        this.productGroupService = productGroupService;
        this.productService = productService;
        this.readableProductGroupPopulator = new ReadableProductGroupPopulator(
                new ReadableMinimalProductPopulator(pricingService, imageUtils));
        this.persistableProductGroupPopulator = persistableProductGroupPopulator;
    }

    @Override
    public ReadableProductGroup getByCode(StoreMerchantId store, String code, LanguageCode language) {
        try {
            return productGroupService.getByCode(store, code)
                    .map(pg -> populateReadable(pg, store, language))
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_GROUP_NOT_FOUND_FOR_CODE_TEMPLATE.formatted(code)));
        } catch (ServiceException e) {
            log.error("Error while fetching ProductGroup by code: {}", code, e);
            throw new ServiceRuntimeException(e);
        }
    }

    private ReadableProductGroup populateReadable(ProductGroup pg, StoreMerchantId store, LanguageCode language) {
        try {
            return readableProductGroupPopulator.populate(pg, new ReadableProductGroup(), store, language);
        } catch (ConversionException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public ReadableProductGroupListV2 list(StoreMerchantId store, LanguageCode language, Pageable pageable) {
        Page<ProductGroup> productGroups = productGroupService.listByStore(store, language, pageable);
        List<ReadableProductGroup> readableList = productGroups.getContent().stream().map(pg -> {
            ReadableProductGroup target = new ReadableProductGroup();
            target.setId(pg.getId());
            target.setCode(pg.getCode());
            target.setActive(pg.isActive());
            return target;
        }).toList();

        ReadableProductGroupListV2 returnList = new ReadableProductGroupListV2();
        returnList.setContent(readableList);
        returnList.setTotalElements(productGroups.getTotalElements());
        returnList.setTotalPages(productGroups.getTotalPages());
        returnList.setPageNumber(productGroups.getNumber());
        returnList.setSize(readableList.size());
        return returnList;
    }

    @Override
    public void delete(StoreMerchantId store, String code) {
        try {
            ProductGroup productGroup = productGroupService.getByCode(store, code)
                    .orElseThrow(() -> new ResourceNotFoundException(PRODUCT_GROUP_NOT_FOUND_FOR_CODE_TEMPLATE.formatted(code)));
            productGroupService.delete(productGroup);
        } catch (ServiceException e) {
            log.error("Error while deleting ProductGroup by code: {}", code, e);
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    @Transactional
    public PersistableProductGroup saveProductGroup(StoreMerchantId store, PersistableProductGroup productGroup) {
        try {
            ProductGroup target = null;
            if (productGroup.getId() != null && productGroup.getId() > 0) {
                target = productGroupService.getById(productGroup.getId());
            }

            if (target == null) {
                target = new ProductGroup();
            }

            persistableProductGroupPopulator.populate(productGroup, target, store, LanguageCode.nonLanguage());
            productGroupService.saveOrUpdate(target);
            productGroup.setId(target.getId());
            return productGroup;
        } catch (ServiceException | ConversionException e) {
            throw new ServiceRuntimeException("Error while saving ProductGroup", e);
        }
    }

    @Override
    @Transactional
    public void addProductToGroup(StoreMerchantId store, String groupCode, Long productId) {
        try {
            ProductGroup group = productGroupService.getByCode(store, groupCode)
                    .orElseThrow(
                            () -> new ResourceNotFoundException(PRODUCT_GROUP_NOT_FOUND_FOR_CODE_TEMPLATE.formatted(groupCode)));
            Product product = productService.getById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found for ID: %s".formatted(productId));
            }
            if (!group.getProducts().contains(product)) {
                group.getProducts().add(product);
                productGroupService.saveOrUpdate(group);
            }
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while adding product to group", e);
        }
    }

    @Override
    @Transactional
    public void removeProductFromGroup(StoreMerchantId store, String groupCode, Long productId) {
        try {
            ProductGroup group = productGroupService.getByCode(store, groupCode)
                    .orElseThrow(
                            () -> new ResourceNotFoundException(PRODUCT_GROUP_NOT_FOUND_FOR_CODE_TEMPLATE.formatted(groupCode)));
            group.getProducts().removeIf(p -> p.getId().equals(productId));
            productGroupService.saveOrUpdate(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while removing product from group", e);
        }
    }

    @Override
    public boolean existByCode(StoreMerchantId store, String code) {
        try {
            return productGroupService.getByCode(store, code).isPresent();
        } catch (ServiceException _) {
            return false;
        }
    }

    @Override
    public ReadableProductGroup getByCodeAndParent(StoreMerchantId store, Long productId, String code,
                                                   LanguageCode language) {
        try {
            return productGroupService.getByParentProductAndCode(store, productId, code)
                    .map(pg -> populateReadable(pg, store, language))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "ProductGroup not found for parent product ID: %s and code: %s".formatted(productId, code)));
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void addProductToGroupForParent(StoreMerchantId store, Long parentProductId, String code, Long productId) {
        try {
            ProductGroup group = productGroupService.getByParentProductAndCode(store, parentProductId, code)
                    .orElseGet(() -> {
                        ProductGroup newGroup = new ProductGroup();
                        newGroup.setCode(code);
                        newGroup.setStoreMerchantId(store);
                        newGroup.setParentProduct(productService.getById(parentProductId));
                        return newGroup;
                    });

            Product product = productService.getById(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found: %s".formatted(productId));
            }

            if (!group.getProducts().contains(product)) {
                group.getProducts().add(product);
                productGroupService.saveOrUpdate(group);
            }
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void removeProductFromGroupForParent(StoreMerchantId store, Long parentProductId, String code,
                                                Long productId) {
        try {
            ProductGroup group = productGroupService.getByParentProductAndCode(store, parentProductId, code)
                    .orElseThrow(() -> new ResourceNotFoundException("ProductGroup not found"));
            group.getProducts().removeIf(p -> p.getId().equals(productId));
            productGroupService.saveOrUpdate(group);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
