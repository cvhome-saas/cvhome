package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariant;
import com.asrevo.cvhome.catalog.entity.product.variant.ProductVariantGroup;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.PersistableProductVariantGroupMapper;
import com.asrevo.cvhome.catalog.service.mapper.catalog.product.ReadableProductVariantGroupMapper;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantGroupService;
import com.asrevo.cvhome.catalog.services.product.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import static com.asrevo.cvhome.store.utils.ReadableEntityUtil.createReadableList;

@Component
public class ProductVariantGroupFacadeImpl implements ProductVariantGroupFacade {

    private final ProductVariantGroupService productVariantGroupService;

    private final ProductVariantService productVariantService;

    private final PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper;

    private final ReadableProductVariantGroupMapper readableProductVariantGroupMapper;

    public ProductVariantGroupFacadeImpl(ProductVariantGroupService productVariantGroupService,
                                         ProductVariantService productVariantService,
                                         PersistableProductVariantGroupMapper persistableProductIntanceGroupMapper,
                                         ReadableProductVariantGroupMapper readableProductVariantGroupMapper) {
        this.productVariantGroupService = productVariantGroupService;
        this.productVariantService = productVariantService;
        this.persistableProductIntanceGroupMapper = persistableProductIntanceGroupMapper;
        this.readableProductVariantGroupMapper = readableProductVariantGroupMapper;
    }

    @Override
    public ReadableProductVariantGroup get(Long instanceGroupId, StoreMerchantId store, LanguageCode language)
            throws ProductVariantGroupNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {

        ProductVariantGroup group = this.group(instanceGroupId, store);
        return readableProductVariantGroupMapper.convert(group, store, language);
    }

    @Override
    public Long create(PersistableProductVariantGroup productVariantGroup, StoreMerchantId store,
                       LanguageCode language) {

        ProductVariantGroup group = persistableProductIntanceGroupMapper.convert(productVariantGroup, store, language);
        group = productVariantGroupService.saveOrUpdate(group);

        return group.getId();
    }

    @Override
    public void update(Long productVariantGroup, PersistableProductVariantGroup instance, StoreMerchantId store,
                       LanguageCode language) throws ProductVariantGroupNotFoundException {
        ProductVariantGroup group = this.group(productVariantGroup, store);
        instance.setId(productVariantGroup);

        group = persistableProductIntanceGroupMapper.merge(instance, group, store, language);

        productVariantGroupService.saveOrUpdate(group);
    }

    @Override
    public void delete(Long productVariantGroup, Long productId, StoreMerchantId store)
            throws ProductVariantGroupNotFoundException, ProductVariantNotFoundException, ServiceException {

        ProductVariantGroup group = this.group(productVariantGroup, store);

        // null all group from instances
        for (ProductVariant instance : group.getProductVariants()) {
            Optional<ProductVariant> p = productVariantService.getById(instance.getId(), store);
            if (p.isEmpty()) {
                throw ProductVariantNotFoundException.of(instance.getId(), store);
            }
            instance.setProductVariantGroup(null);
            productVariantService.save(instance);
        }

        // now delete
        productVariantGroupService.delete(group);
    }

    @Override
    public ReadableEntityList<ReadableProductVariantGroup> list(Long productId, StoreMerchantId store,
                                                                LanguageCode language, Pageable pageable)
            throws ProductVariantParentMissingException, InventoryNotConvertibleException {

        Page<ProductVariantGroup> groups = productVariantGroupService.getByProductId(store, productId, language,
                pageable);

        // A plain loop rather than stream().map(...): the group mapper declares checked failures now.
        List<ReadableProductVariantGroup> readableInstances = new ArrayList<>();
        for (ProductVariantGroup group : groups) {
            readableInstances.add(readableProductVariantGroupMapper.convert(group, store, language));
        }

        return createReadableList(groups, readableInstances);
    }

    private ProductVariantGroup group(Long productOptionGroupId, StoreMerchantId store)
            throws ProductVariantGroupNotFoundException {
        return productVariantGroupService.getById(productOptionGroupId, store)
                .orElseThrow(() -> ProductVariantGroupNotFoundException.of(productOptionGroupId, store));
    }

}
