package com.asrevo.cvhome.billing.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.PlanPriceId;
import com.asrevo.cvhome.billing.commons.StripeCustomerId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.StripeInvoiceId;
import com.asrevo.cvhome.billing.commons.StripePriceId;
import com.asrevo.cvhome.billing.commons.StripeProductId;
import com.asrevo.cvhome.billing.commons.StripeScheduleId;
import com.asrevo.cvhome.billing.commons.StripeSubscriptionId;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Teaches Spring Data JDBC to read and write the identifier value objects this service stores as columns.
 *
 * <p>
 * One writing converter covers every {@link Identifier}; each type then needs its own reading converter, because the
 * target type is what selects it. A missing reading converter is not a compile error — it surfaces as a
 * {@code ConverterNotFoundException} the first time that column is queried, which is why the list below is kept in
 * step with the columns by hand.
 * </p>
 */
@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    @Override
    protected List<?> userConverters() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new Converter<Identifier, String>() {
            @Override
            public String convert(Identifier source) {
                return source.getId().toString();
            }
        });
        converters.add(new Converter<String, ManagerOrgId>() {
            @Override
            public ManagerOrgId convert(String source) {
                return new ManagerOrgId(source);
            }
        });
        converters.add(new Converter<String, StoreMerchantId>() {
            @Override
            public StoreMerchantId convert(String source) {
                return new StoreMerchantId(source);
            }
        });
        converters.add(new Converter<String, PlanId>() {
            @Override
            public PlanId convert(String source) {
                return new PlanId(source);
            }
        });
        converters.add(new Converter<String, PlanPriceId>() {
            @Override
            public PlanPriceId convert(String source) {
                return new PlanPriceId(source);
            }
        });
        converters.add(new Converter<String, StripeCustomerId>() {
            @Override
            public StripeCustomerId convert(String source) {
                return new StripeCustomerId(source);
            }
        });
        converters.add(new Converter<String, StripeSubscriptionId>() {
            @Override
            public StripeSubscriptionId convert(String source) {
                return new StripeSubscriptionId(source);
            }
        });
        converters.add(new Converter<String, StripeScheduleId>() {
            @Override
            public StripeScheduleId convert(String source) {
                return new StripeScheduleId(source);
            }
        });
        converters.add(new Converter<String, StripeEventId>() {
            @Override
            public StripeEventId convert(String source) {
                return new StripeEventId(source);
            }
        });
        converters.add(new Converter<String, StripeInvoiceId>() {
            @Override
            public StripeInvoiceId convert(String source) {
                return new StripeInvoiceId(source);
            }
        });
        converters.add(new Converter<String, StripePriceId>() {
            @Override
            public StripePriceId convert(String source) {
                return new StripePriceId(source);
            }
        });
        converters.add(new Converter<String, StripeProductId>() {
            @Override
            public StripeProductId convert(String source) {
                return new StripeProductId(source);
            }
        });
        converters.add(new Converter<String, CurrencyCode>() {
            @Override
            public CurrencyCode convert(String source) {
                return new CurrencyCode(source);
            }
        });
        converters.add(new Converter<CurrencyCode, String>() {
            @Override
            public String convert(CurrencyCode source) {
                return source.code();
            }
        });
        return converters;
    }

}
