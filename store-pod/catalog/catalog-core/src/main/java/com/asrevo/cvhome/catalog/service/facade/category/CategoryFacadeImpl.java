package com.asrevo.cvhome.catalog.service.facade.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategoryList;
import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.catalog.model.product.attribute.ReadableProductVariantValue;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableCategoryMapper;
import com.asrevo.cvhome.catalog.service.populator.catalog.PersistableCategoryPopulator;
import com.asrevo.cvhome.catalog.service.populator.catalog.ReadableCategoryPopulator;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.attribute.ProductAttributeService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.controller.exception.OperationNotAllowedException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.exception.ConversionException;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service(value = "categoryFacade")
@AllArgsConstructor
@Slf4j
public class CategoryFacadeImpl implements CategoryFacade {

    private static final String FEATURED_CATEGORY = "featured";
    private static final String VISIBLE_CATEGORY = "visible";
    private static final String ADMIN_CATEGORY = "admin";
    private final CategoryService categoryService;
    private final PersistableCategoryPopulator persistableCatagoryPopulator;
    private final ReadableCategoryMapper readableCategoryMapper;
    private final ProductAttributeService productAttributeService;
    private final ExternalMerchantStoreService externalMerchantStoreService;

    @Override
    public ReadableCategoryList getCategoryHierarchy(
            StoreMerchantId store,
            ListCriteria criteria,
            int depth,
            LanguageCode language,
            List<String> filter,
            int page,
            int count) {

        Assert.notNull(store, "Store can not be null");

        // get parent store

        ReadableCategoryList returnList =
                getReadableCategoryList(store, criteria, depth, language, filter, page, count);

        Map<Long, ReadableCategory> readableCategoryMap =
                returnList.getContent().stream()
                        .collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));

        returnList.getContent().stream()
                // .filter(ReadableCategory::isVisible)
                .filter(cat -> Objects.nonNull(cat.getParent()))
                .filter(cat -> readableCategoryMap.containsKey(cat.getParent().getId()))
                .forEach(
                        readableCategory -> {
                            ReadableCategory parentCategory =
                                    readableCategoryMap.get(readableCategory.getParent().getId());
                            if (parentCategory != null) {
                                parentCategory.getChildren().add(readableCategory);
                            }
                        });

        List<ReadableCategory> filteredList = new ArrayList<>(readableCategoryMap.values());

        // execute only if not admin filtered
        if (filter == null || !filter.contains(ADMIN_CATEGORY)) {
            filteredList =
                    readableCategoryMap.values().stream()
                            .filter(cat -> cat.getDepth() == 0)
                            .sorted(Comparator.comparing(ReadableCategory::getSortOrder))
                            .collect(Collectors.toList());

            returnList.setSize(filteredList.size());
        }

        returnList.setContent(filteredList);

        return returnList;
    }

    @Override
    public ReadableCategoryList getReadableCategoryList(
            StoreMerchantId store,
            ListCriteria criteria,
            int depth,
            LanguageCode language,
            List<String> filter,
            int page,
            int count) {
        List<Category> categories;
        ReadableCategoryList returnList = new ReadableCategoryList();
        if (!CollectionUtils.isEmpty(filter) && filter.contains(FEATURED_CATEGORY)) {
            categories = categoryService.getListByDepthFilterByFeatured(store, depth, language);
            returnList.setTotalElements(categories.size());
            returnList.setSize(categories.size());
            returnList.setTotalPages(1);
        } else {
            org.springframework.data.domain.Page<Category> pageable =
                    categoryService.getListByDepth(
                            store,
                            language,
                            criteria != null ? criteria.getName() : null,
                            depth,
                            page,
                            count);
            categories = pageable.getContent();
            returnList.setTotalElements(pageable.getTotalElements());
            returnList.setTotalPages(pageable.getTotalPages());
            returnList.setSize(categories.size());
        }

        if (filter != null && filter.contains(VISIBLE_CATEGORY)) {
            List<ReadableCategory> categoryList =
                    categories.stream()
                            .filter(Category::isVisible)
                            .map(cat -> readableCategoryMapper.convert(cat, store, language))
                            .collect(Collectors.toList());
            returnList.setContent(categoryList);
        } else {
            List<ReadableCategory> categoryList =
                    categories.stream()
                            .map(cat -> readableCategoryMapper.convert(cat, store, language))
                            .collect(Collectors.toList());
            returnList.setContent(categoryList);
        }
        return returnList;
    }

    @Override
    public boolean existByCode(StoreMerchantId store, String code) {
        try {
            Category c = categoryService.getByCode(store, code);
            return c != null;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public PersistableCategory saveCategory(StoreMerchantId store, PersistableCategory category) {
        try {

            Long categoryId = category.getId();
            Category target =
                    Optional.ofNullable(categoryId)
                            .filter(merchant -> store != null)
                            .filter(id -> id > 0)
                            .map(categoryService::getById)
                            .orElse(new Category());

            Category dbCategory = populateCategory(store, category, target);
            saveCategory(store, dbCategory, null);

            // set category id
            category.setId(dbCategory.getId());
            return category;
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while updating category", e);
        }
    }

    private Category populateCategory(
            StoreMerchantId store, PersistableCategory category, Category target) {
        try {
            LanguageCode defaultLanguage =
                    externalMerchantStoreService.getStore(store).getDefaultLanguage();
            return persistableCatagoryPopulator.populate(category, target, store, defaultLanguage);
        } catch (ConversionException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private void saveCategory(StoreMerchantId store, Category category, Category parent)
            throws ServiceException {

        if (parent != null) {
            category.setParent(category);

            String lineage = parent.getLineage();
            int depth = parent.getDepth();

            category.setDepth(depth + 1);
            category.setLineage(lineage); // service
            // will
            // adjust
            // lineage
        }

        category.setStoreMerchantId(store);

        // remove children
        List<Category> children = category.getCategories();
        List<Category> saveAfter =
                children.stream()
                        .filter(c -> c.getId() == null || c.getId() == 0)
                        .collect(Collectors.toList());
        List<Category> saveNow =
                children.stream()
                        .filter(c -> c.getId() != null && c.getId() > 0)
                        .collect(Collectors.toList());
        category.setCategories(saveNow);

        if (parent != null) {
            category.setParent(parent);
        }

        categoryService.saveOrUpdate(category);

        if (!CollectionUtils.isEmpty(saveAfter)) {
            parent = category;
            for (Category c : saveAfter) {
                if (c.getId() == null || c.getId() == 0) {
                    for (Category sub : children) {
                        saveCategory(store, sub, parent);
                    }
                }
            }
        }
    }

    @Override
    public ReadableCategory getById(StoreMerchantId store, Long id, LanguageCode language) {

        Category categoryModel;
        if (language != null) {
            categoryModel = getCategoryById(id, language);
        } else { // all langs
            categoryModel = getById(store, id);
        }

        if (categoryModel == null)
            throw new ResourceNotFoundException("Categori id [" + id + "] not found");

        String lineage = categoryModel.getLineage();

        ReadableCategory readableCategory =
                readableCategoryMapper.convert(categoryModel, store, language);

        // get children
        List<Category> children = getListByLineage(store, lineage);

        List<ReadableCategory> childrenCats =
                children.stream()
                        .map(cat -> readableCategoryMapper.convert(cat, store, language))
                        .collect(Collectors.toList());

        addChildToParent(readableCategory, childrenCats);
        return readableCategory;
    }

    private void addChildToParent(
            ReadableCategory readableCategory, List<ReadableCategory> childrenCats) {
        Map<Long, ReadableCategory> categoryMap =
                childrenCats.stream()
                        .collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));
        categoryMap.put(readableCategory.getId(), readableCategory);

        // traverse map and add child to parent
        for (ReadableCategory readable : childrenCats) {

            if (readable.getParent() != null) {

                ReadableCategory rc = categoryMap.get(readable.getParent().getId());
                if (rc != null) {
                    rc.getChildren().add(readable);
                }
            }
        }
    }

    private List<Category> getListByLineage(StoreMerchantId store, String lineage) {
        try {
            return categoryService.getListByLineage(store, lineage);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    String.format("Error while getting root category %s", e.getMessage()), e);
        }
    }

    private Category getCategoryById(Long id, LanguageCode language) {
        return Optional.ofNullable(categoryService.getOneByLanguage(id, language))
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category id [" + id + "] not found"));
    }

    private void deleteCategory(Category category) {
        try {
            categoryService.delete(category);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException("Error while deleting category", e);
        }
    }

    @Override
    public ReadableCategory getCategoryByFriendlyUrl(
            StoreMerchantId store, String friendlyUrl, LanguageCode language) {
        Assert.notNull(friendlyUrl, "Category search friendly URL must not be null");

        Category category = categoryService.getBySeUrl(store, friendlyUrl, language);

        if (category == null) {
            throw new ResourceNotFoundException(
                    "Category with friendlyUrl [" + friendlyUrl + "] was not found");
        }

        ReadableCategoryPopulator categoryPopulator = new ReadableCategoryPopulator();
        ReadableCategory readableCategory = new ReadableCategory();

        categoryPopulator.populate(category, readableCategory, store, language);

        return readableCategory;
    }

    private Category getById(StoreMerchantId store, Long id) {
        Assert.notNull(id, "category id must not be null");
        Assert.notNull(store, "store cannot be null");
        Category category = categoryService.getById(id, store);
        if (category == null) {
            throw new ResourceNotFoundException("Category with id [" + id + "] not found");
        }
        if (!Objects.equals(category.getStoreMerchantId(), store)) {
            throw new UnauthorizedException("Unauthorized");
        }
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId, StoreMerchantId store) {
        Category category = getOne(categoryId, store);
        deleteCategory(category);
    }

    private Category getOne(Long categoryId, StoreMerchantId storeMerchantId) {
        return Optional.ofNullable(categoryService.getById(categoryId))
                .filter(it -> it.getStoreMerchantId().equals(storeMerchantId))
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        String.format(
                                                "No Category found for ID : %s", categoryId)));
    }

    @Override
    public List<ReadableProductVariant> categoryProductVariants(
            Long categoryId, StoreMerchantId store, LanguageCode language) {
        Category category = categoryService.getById(categoryId, store);

        List<ReadableProductVariant> variants = new ArrayList<>();

        if (category == null) {
            throw new ResourceNotFoundException("Category [" + categoryId + "] not found");
        }

        try {
            List<ProductAttribute> attributes =
                    productAttributeService.getProductAttributesByCategoryLineage(
                            store, category.getLineage(), language);

            Map<String, List<ProductOptionValue>> rawFacet = new HashMap<>();
            Map<String, ProductOption> references = new HashMap<>();
            for (ProductAttribute attr : attributes) {
                references.put(attr.getProductOption().getCode(), attr.getProductOption());
                List<ProductOptionValue> values =
                        rawFacet.computeIfAbsent(
                                attr.getProductOption().getCode(), k -> new ArrayList<>());

                if (attr.getProductOptionValue() != null) {
                    Optional<ProductOptionValueDescription> desc =
                            attr.getProductOptionValue().getDescriptions().stream()
                                    .filter(o -> o.getLanguageCode().equals(language))
                                    .findFirst();

                    ProductOptionValue val = new ProductOptionValue();
                    val.setCode(attr.getProductOption().getCode());
                    String order = attr.getAttributeSortOrder();
                    val.setSortOrder(
                            order == null
                                    ? attr.getId().intValue()
                                    : Integer.parseInt(attr.getAttributeSortOrder()));
                    if (desc.isPresent()) {
                        val.setName(desc.get().getName());
                    } else {
                        val.setName(attr.getProductOption().getCode());
                    }
                    values.add(val);
                }
            }

            // for each reference set Option
            for (Entry<String, ProductOption> pair : references.entrySet()) {
                ProductOption option = pair.getValue();
                List<ProductOptionValue> values = rawFacet.get(option.getCode());

                ReadableProductVariant productVariant = new ReadableProductVariant();
                Optional<ProductOptionDescription> optionDescription =
                        option.getDescriptions().stream()
                                .filter(o -> o.getLanguageCode().equals(language))
                                .findFirst();
                if (optionDescription.isPresent()) {
                    productVariant.setName(optionDescription.get().getName());
                    productVariant.setId(optionDescription.get().getId());
                    productVariant.setCode(optionDescription.get().getProductOption().getCode());
                    List<ReadableProductVariantValue> optionValues = new ArrayList<>();
                    for (ProductOptionValue value : values) {
                        ReadableProductVariantValue v = new ReadableProductVariantValue();
                        v.setCode(value.getCode());
                        v.setName(value.getName());
                        v.setDescription(value.getName());
                        v.setOption(option.getId());
                        v.setValue(value.getId());
                        v.setOrder(option.getProductOptionSortOrder());
                        optionValues.add(v);
                    }

                    Comparator<ReadableProductVariantValue> orderComparator =
                            Comparator.comparingInt(ReadableProductVariantValue::getOrder);

                    // Arrays.sort(employees, employeeSalaryComparator);

                    List<ReadableProductVariantValue> readableValues;

                    // sort by name
                    // remove duplicates
                    readableValues = optionValues.stream().distinct().collect(Collectors.toList());
                    readableValues.sort(Comparator.comparing(ReadableProductVariantValue::getName));

                    productVariant.setOptions(readableValues);
                    variants.add(productVariant);
                }
            }

            return variants;
        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "An error occured while retrieving ProductAttributes", e);
        }
    }

    @Override
    public void move(Long child, Long parent, StoreMerchantId store) {

        Assert.notNull(child, "Child category must not be null");
        Assert.notNull(parent, "Parent category must not be null");
        Assert.notNull(store, "Merhant must not be null");

        try {

            Category c = categoryService.getById(child, store);

            if (c == null) {
                throw new ResourceNotFoundException(
                        "Category with id [" + child + "] for store [" + store + "]");
            }

            if (parent == -1) {
                categoryService.addChild(null, c);
                return;
            }

            Category p = categoryService.getById(parent, store);

            if (p == null) {
                throw new ResourceNotFoundException(
                        "Category with id [" + parent + "] for store [" + store + "]");
            }

            if (c.getParent() != null && c.getParent().getId().equals(parent)) {
                return;
            }

            if (!Objects.equals(c.getStoreMerchantId(), store)) {
                throw new OperationNotAllowedException(
                        "Invalid identifiers for Merchant [" + c.getStoreMerchantId() + "]");
            }

            if (!Objects.equals(p.getStoreMerchantId(), store)) {
                throw new OperationNotAllowedException(
                        "Invalid identifiers for Merchant [" + c.getStoreMerchantId() + "]");
            }

            p.getAuditSection().setModifiedBy("Api");
            categoryService.addChild(p, c);
        } catch (ResourceNotFoundException | OperationNotAllowedException re) {
            throw re;
        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @Override
    public Category getByCode(String code, StoreMerchantId store) {
        try {
            return categoryService.getByCode(store, code);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(
                    "Exception while reading category code [" + code + "]", e);
        }
    }

    @Override
    public void setVisible(PersistableCategory category, StoreMerchantId store) {
        Assert.notNull(category, "Category must not be null");
        Assert.notNull(store, "Store must not be null");
        try {
            Category c = this.getById(store, category.getId());
            c.setVisible(category.isVisible());
            categoryService.saveOrUpdate(c);
        } catch (Exception e) {
            throw new ServiceRuntimeException(
                    "Error while getting category [" + category.getId() + "]", e);
        }
    }

    @Override
    public ReadableCategoryList listByProduct(
            StoreMerchantId store, Long product, LanguageCode language) {
        Assert.notNull(product, "Product id must not be null");
        Assert.notNull(store, "Store must not be null");

        List<Category> categories = categoryService.getByProductId(product, store);

        List<ReadableCategory> readableCategories =
                categories.stream()
                        .map(cat -> readableCategoryMapper.convert(cat, store, language))
                        .collect(Collectors.toList());

        ReadableCategoryList readableList = new ReadableCategoryList();
        readableList.setContent(readableCategories);
        readableList.setTotalPages(1);
        readableList.setSize(readableCategories.size());
        readableList.setTotalElements(readableCategories.size());

        return readableList;
    }
}
