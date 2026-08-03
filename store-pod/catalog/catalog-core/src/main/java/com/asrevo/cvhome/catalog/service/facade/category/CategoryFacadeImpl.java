package com.asrevo.cvhome.catalog.service.facade.category;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductAttribute;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionDescription;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValueDescription;
import com.asrevo.cvhome.catalog.errors.CategoryDescriptionLanguageMissingException;
import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryIdentifiersInconsistentException;
import com.asrevo.cvhome.catalog.errors.CategoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ForeignStoreProductAccessException;
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
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service(value = "categoryFacade")
@AllArgsConstructor
@Slf4j
public class CategoryFacadeImpl implements CategoryFacade {

    private static final String FEATURED_CATEGORY = "featured";

    private static final String VISIBLE_CATEGORY = "visible";

    private static final String ADMIN_CATEGORY = "admin";

    private static final String CATEGORY_ID_NOT_FOUND_TEMPLATE = "Category id [%s] not found";

    private static final String CATEGORY_WITH_ID_NOT_FOUND_TEMPLATE = "Category with id [%s] not found";

    private static final String CATEGORY_NOT_FOUND_TEMPLATE = "Category [%s] not found";

    private static final String CATEGORY_WITH_ID_FOR_STORE_TEMPLATE = "Category with id [%s] for store [%s]";

    private static final String INVALID_IDENTIFIERS_TEMPLATE = "Invalid identifiers for Merchant [%s]";

    private static final String READING_CATEGORY_CODE_ERROR_TEMPLATE = "Exception while reading category code [%s]";

    private static final String GETTING_CATEGORY_ERROR_TEMPLATE = "Error while getting category [%s]";

    private final CategoryService categoryService;

    private final PersistableCategoryPopulator persistableCatagoryPopulator;

    private final ReadableCategoryMapper readableCategoryMapper;

    private final ProductAttributeService productAttributeService;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    @Override
    public ReadableCategoryList getCategoryHierarchy(StoreMerchantId store, ListCriteria criteria, int depth,
                                                     LanguageCode language, List<String> filter, Pageable pageable) {
        ReadableCategoryList returnList = getReadableCategoryList(store, criteria, depth, language, filter, pageable);

        Map<Long, ReadableCategory> readableCategoryMap = returnList.getContent()
                .stream()
                .collect(Collectors.toMap(ReadableCategory::getId, Function.identity()));

        returnList.getContent()
                .stream()
                // .filter(ReadableCategory::isVisible)
                .filter(cat -> Objects.nonNull(cat.getParent()))
                .filter(cat -> readableCategoryMap.containsKey(cat.getParent().getId()))
                .forEach(readableCategory -> {
                    ReadableCategory parentCategory = readableCategoryMap.get(readableCategory.getParent().getId());
                    if (parentCategory != null) {
                        parentCategory.getChildren().add(readableCategory);
                    }
                });

        List<ReadableCategory> filteredList = new ArrayList<>(readableCategoryMap.values());

        returnList.setContent(filteredList.stream().filter(it -> it.getDepth() == 0).toList());

        return returnList;
    }

    @Override
    public ReadableCategoryList getReadableCategoryList(StoreMerchantId store, ListCriteria criteria, int depth,
                                                        LanguageCode language, List<String> filter, Pageable pageable) {
        List<Category> categories;
        ReadableCategoryList returnList = new ReadableCategoryList();
        org.springframework.data.domain.Page<Category> page = categoryService.getListByDepth(store, language,
                criteria != null ? criteria.getName() : null, depth, pageable);
        categories = page.getContent();
        returnList.setTotalElements(page.getTotalElements());
        returnList.setTotalPages(page.getTotalPages());
        returnList.setSize(categories.size());
        returnList.setPageNumber(page.getNumber());

        List<ReadableCategory> categoryList = categories.stream()
                .map(cat -> readableCategoryMapper.convert(cat, store, language))
                .toList();
        returnList.setContent(categoryList);
        return returnList;
    }

    @Override
    public boolean existByCode(StoreMerchantId store, String code) {
        return categoryService.getByCode(store, code) != null;
    }

    @Override
    public PersistableCategory saveCategory(StoreMerchantId store, PersistableCategory category)
            throws ServiceException, CategoryNotConvertibleException, CategoryReferenceUnresolvableException,
            CategoryDescriptionLanguageMissingException {

            Long categoryId = category.getId();
            Category target = Optional.ofNullable(categoryId)
                    .filter(merchant -> store != null)
                    .filter(id -> id > 0)
                    .map(categoryService::getById)
                    .orElse(new Category());

            Category dbCategory = populateCategory(store, category, target);
            saveCategory(store, dbCategory, null);

            // set category id
            category.setId(dbCategory.getId());
            return category;
    }

    private Category populateCategory(StoreMerchantId store, PersistableCategory category, Category target)
            throws CategoryNotConvertibleException, CategoryReferenceUnresolvableException,
            CategoryDescriptionLanguageMissingException {
        LanguageCode defaultLanguage = externalMerchantStoreService.getStore(store).getDefaultLanguage();
        return persistableCatagoryPopulator.populate(category, target, store, defaultLanguage);
    }

    private void saveCategory(StoreMerchantId store, Category category, Category parent) throws ServiceException {

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
        List<Category> saveAfter = children.stream()
                .filter(c -> c.getId() == null || c.getId() == 0)
                .toList();
        List<Category> saveNow = children.stream()
                .filter(c -> c.getId() != null && c.getId() > 0)
                .toList();
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
    public ReadableCategory getById(StoreMerchantId store, Long id, LanguageCode language)
            throws CategoryNotFoundException, CategoryNotConvertibleException {
        Category category = categoryService.getById(id, store);

        if (category == null) {
            throw CategoryNotFoundException.of(id, store);
        }

        String lineage = category.getLineage();

        ReadableCategory readableCategory = readableCategoryMapper.convert(category, store, language);

        // A plain loop rather than stream().map(...): the mapper declares a checked failure now.
        List<ReadableCategory> children = new ArrayList<>();
        for (Category child : categoryService.getListByLineage(store, lineage)) {
            children.add(readableCategoryMapper.convert(child, store, LanguageCode.nonLanguage()));
        }
        readableCategory.setChildren(children);
        return readableCategory;
    }

    private void deleteCategory(Category category) throws ServiceException {
        categoryService.delete(category);
    }

    @Override
    public ReadableCategory getCategoryByFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws CategoryFriendlyUrlNotFoundException {
        Category category = categoryService.getBySeUrl(store, friendlyUrl, language);

        if (category == null) {
            throw CategoryFriendlyUrlNotFoundException.of(friendlyUrl, store);
        }

        ReadableCategoryPopulator categoryPopulator = new ReadableCategoryPopulator();
        ReadableCategory readableCategory = new ReadableCategory();

        categoryPopulator.populate(category, readableCategory, store, language);

        return readableCategory;
    }

    private Category getById(StoreMerchantId store, Long id)
            throws CategoryNotFoundException, ForeignStoreProductAccessException {
        Category category = categoryService.getById(id, store);
        if (category == null) {
            throw CategoryNotFoundException.of(id, store);
        }
        if (!Objects.equals(category.getStoreMerchantId(), store)) {
            // Was UnauthorizedException, a bare 401 whose message was the single word "Unauthorized". The caller is
            // authenticated; the category belongs to another store, which is a 403.
            throw ForeignStoreProductAccessException.of(id, store);
        }
        return category;
    }

    @Override
    public void deleteCategory(Long categoryId, StoreMerchantId store)
            throws CategoryNotFoundException, ServiceException {
        Category category = getOne(categoryId, store);
        deleteCategory(category);
    }

    private Category getOne(Long categoryId, StoreMerchantId storeMerchantId) throws CategoryNotFoundException {
        return Optional.ofNullable(categoryService.getById(categoryId))
                .filter(it -> it.getStoreMerchantId().equals(storeMerchantId))
                .orElseThrow(() -> CategoryNotFoundException.of(categoryId, storeMerchantId));
    }

    @Override
    public List<ReadableProductVariant> categoryProductVariants(Long categoryId, StoreMerchantId store,
                                                                LanguageCode language)
            throws CategoryNotFoundException {
        Category category = categoryService.getById(categoryId, store);

        List<ReadableProductVariant> variants = new ArrayList<>();

        if (category == null) {
            throw CategoryNotFoundException.of(categoryId, store);
        }

        {
            List<ProductAttribute> attributes = productAttributeService.getProductAttributesByCategoryLineage(store,
                    category.getLineage(), language);

            Map<String, List<ProductOptionValue>> rawFacet = new HashMap<>();
            Map<String, ProductOption> references = new HashMap<>();
            for (ProductAttribute attr : attributes) {
                references.put(attr.getProductOption().getCode(), attr.getProductOption());
                List<ProductOptionValue> values = rawFacet.computeIfAbsent(attr.getProductOption().getCode(),
                        k -> new ArrayList<>());

                if (attr.getProductOptionValue() != null) {
                    Optional<ProductOptionValueDescription> desc = attr.getProductOptionValue()
                            .getDescriptions()
                            .stream()
                            .filter(o -> o.getLanguageCode().equals(language))
                            .findFirst();

                    ProductOptionValue val = new ProductOptionValue();
                    val.setCode(attr.getProductOption().getCode());
                    String order = attr.getAttributeSortOrder();
                    val.setSortOrder(
                            order == null ? attr.getId().intValue() : Integer.parseInt(attr.getAttributeSortOrder()));
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
                Optional<ProductOptionDescription> optionDescription = option.getDescriptions()
                        .stream()
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


                    List<ReadableProductVariantValue> readableValues;

                    // sort by name
                    // remove duplicates
                    readableValues = optionValues.stream().distinct().toList();
                    readableValues.sort(Comparator.comparing(ReadableProductVariantValue::getName));

                    productVariant.setOptions(readableValues);
                    variants.add(productVariant);
                }
            }

            return variants;
        }
    }

    @Override
    public void move(Long child, Long parent, StoreMerchantId store)
            throws CategoryNotFoundException, CategoryIdentifiersInconsistentException,
            CategoryReferenceUnresolvableException, ServiceException {

            Category c = categoryService.getById(child, store);

            if (c == null) {
                throw CategoryNotFoundException.of(child, store);
            }

            if (parent == -1) {
                categoryService.addChild(null, c);
                return;
            }

            Category p = categoryService.getById(parent, store);

            if (p == null) {
                throw CategoryNotFoundException.of(parent, store);
            }

            if (c.getParent() != null && c.getParent().getId().equals(parent)) {
                return;
            }

        if (!Objects.equals(c.getStoreMerchantId(), store) || !Objects.equals(p.getStoreMerchantId(), store)) {
            throw CategoryIdentifiersInconsistentException.of(store);
            }

            p.getAuditSection().setModifiedBy("Api");
            categoryService.addChild(p, c);
    }

    @Override
    public Category getByCode(String code, StoreMerchantId store) {
        return categoryService.getByCode(store, code);
    }

    @Override
    public void setVisible(PersistableCategory category, StoreMerchantId store)
            throws CategoryNotFoundException, ForeignStoreProductAccessException, ServiceException {
        Category c = this.getById(store, category.getId());
        c.setVisible(category.isVisible());
        categoryService.saveOrUpdate(c);
    }

    @Override
    public ReadableCategoryList listByProduct(StoreMerchantId store, Long product, LanguageCode language)
            throws CategoryNotConvertibleException {
        List<Category> categories = categoryService.getByProductId(product, store);

        List<ReadableCategory> readableCategories = new ArrayList<>();
        for (Category cat : categories) {
            readableCategories.add(readableCategoryMapper.convert(cat, store, language));
        }

        ReadableCategoryList readableList = new ReadableCategoryList();
        readableList.setContent(readableCategories);
        readableList.setTotalPages(1);
        readableList.setSize(readableCategories.size());
        readableList.setTotalElements(readableCategories.size());

        return readableList;
    }

}
