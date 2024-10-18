package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.*;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CatalogServiceHelper {

    /**
     * Filters descriptions and set the appropriate language
     *
     */
    public static void setToLanguage(Product p, int language) {

        Set<ProductAttribute> attributes = p.getAttributes();
        if (attributes != null) {

            for (ProductAttribute attribute : attributes) {

                ProductOption po = attribute.getProductOption();
                if (po.getDescriptions() != null) {
                    Set<ProductOptionDescription> podDescriptions =
                            po.getDescriptions().stream()
                                    .filter(pod -> pod.getLanguage().getId() == language)
                                    .collect(Collectors.toSet());
                    po.setDescriptions(podDescriptions);
                }

                ProductOptionValue pov = attribute.getProductOptionValue();
                if (pov.getDescriptions() != null) {
                    Set<ProductOptionValueDescription> povdDescriptions =
                            pov.getDescriptions().stream()
                                    .filter(pod -> pod.getLanguage().getId() == language)
                                    .collect(Collectors.toSet());
                    pov.setDescriptions(povdDescriptions);
                }
            }
        }
    }

    /**
     * Overwrites the availability in order to return 1 price / region
     *
     */
    public static void setToAvailability(Product product, Locale locale) {

        Set<ProductAvailability> availabilities = product.getAvailabilities();
        Set<ProductAvailability> productAvailabilities = new HashSet<>();

        Optional<ProductAvailability> defaultAvailability =
                availabilities.stream()
                        .filter(
                                productAvailability ->
                                        productAvailability
                                                .getRegion()
                                                .equals(Constants.ALL_REGIONS))
                        .findFirst();
        Optional<ProductAvailability> localeAvailability =
                availabilities.stream()
                        .filter(
                                productAvailability ->
                                        productAvailability.getRegion().equals(locale.getCountry()))
                        .findFirst();
        defaultAvailability.ifPresent(productAvailabilities::add);
        localeAvailability.ifPresent(productAvailabilities::add);

        if (productAvailabilities.isEmpty()) {
            product.setAvailabilities(productAvailabilities);
        }
    }
}
