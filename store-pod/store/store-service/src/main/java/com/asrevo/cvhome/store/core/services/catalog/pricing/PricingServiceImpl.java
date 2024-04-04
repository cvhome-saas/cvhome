package com.asrevo.cvhome.store.core.services.catalog.pricing;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.availability.ProductAvailability;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.ProductPriceUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Contains all the logic required to calculate product price
 *
 * @author Carl Samson
 */
@Service("pricingService")
@Slf4j
public class PricingServiceImpl implements PricingService {


    @Autowired
    private ProductPriceUtils priceUtil;

    @Override
    public FinalPrice calculateProductPrice(Product product) throws ServiceException {
        return priceUtil.getFinalPrice(product);
    }

    @Override
    public FinalPrice calculateProductPrice(Product product, Customer customer) throws ServiceException {
        /** TODO add rules for price calculation **/
        return priceUtil.getFinalPrice(product);
    }

    @Override
    public FinalPrice calculateProductPrice(Product product, List<ProductAttribute> attributes) throws ServiceException {
        return priceUtil.getFinalPrice(product, attributes);
    }

    @Override
    public FinalPrice calculateProductPrice(Product product, List<ProductAttribute> attributes, Customer customer) throws ServiceException {
        /** TODO add rules for price calculation **/
        return priceUtil.getFinalPrice(product, attributes);
    }

    @Override
    public BigDecimal calculatePriceQuantity(BigDecimal price, int quantity) {
        return price.multiply(new BigDecimal(quantity));
    }

    @Override
    public String getDisplayAmount(BigDecimal amount, MerchantStore store) throws ServiceException {
        try {
            return priceUtil.getStoreFormatedAmountWithCurrency(store, amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amount " + amount.toString());
            throw new ServiceException(e);
        }
    }

    @Override
    public String getDisplayAmount(BigDecimal amount, Locale locale,
                                   Currency currency, MerchantStore store) throws ServiceException {
        try {
            return priceUtil.getFormatedAmountWithCurrency(locale, currency, amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amunt " + amount.toString() + " using locale " + locale.toString() + " and currency " + currency.toString());
            throw new ServiceException(e);
        }
    }

    @Override
    public String getStringAmount(BigDecimal amount, MerchantStore store)
            throws ServiceException {
        try {
            return priceUtil.getAdminFormatedAmount(store, amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amount " + amount.toString());
            throw new ServiceException(e);
        }
    }

    @Override
    public BigDecimal getAmount(String amount) throws ServiceException {

        try {
            return priceUtil.getAmount(amount);
        } catch (Exception e) {
            log.error("An error occured when trying to format an amount " + amount);
            throw new ServiceException(e);
        }

    }

    @Override
    public FinalPrice calculateProductPrice(ProductAvailability availability) throws ServiceException {

        return priceUtil.getFinalPrice(availability);
    }

    @Override
    public FinalPrice calculateProductPrice(ProductVariant variant) throws ServiceException {
        // TODO Auto-generated method stub
        return null;
    }


}
