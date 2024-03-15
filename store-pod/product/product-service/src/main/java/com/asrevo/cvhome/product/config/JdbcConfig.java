package com.asrevo.cvhome.product.config;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.product.commons.domain.CategoryId;
import com.asrevo.cvhome.product.commons.domain.ImageLink;
import com.asrevo.cvhome.product.commons.domain.ProductId;
import com.asrevo.cvhome.product.commons.domain.ProductVariantId;
import com.asrevo.cvhome.product.config.converters.JsonToMapConverter;
import com.asrevo.cvhome.product.config.converters.JsonToProductDetailsConverter;
import com.asrevo.cvhome.product.config.converters.MapToJsonConverter;
import com.asrevo.cvhome.product.config.converters.ProductDetailsToJsonConverter;
import com.asrevo.cvhome.store.commons.domain.StoreId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    private final ObjectMapper ployJson;
    @Autowired
    private ObjectMapper mapper;

    public JdbcConfig() {
        this.ployJson = JacksonConfig.getPloyJson();
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new MapToJsonConverter(mapper));
        converters.add(new JsonToMapConverter(mapper));
        converters.add(new ProductDetailsToJsonConverter(mapper));
        converters.add(new JsonToProductDetailsConverter(mapper));
        converters.add(new Converter<Identifier, String>() {
            @Override
            public String convert(Identifier source) {
                return source.getId().toString();
            }
        });
        converters.add(new Converter<Currency, String>() {
            @Override
            public String convert(Currency source) {
                return source.toString();
            }
        });
        converters.add(new Converter<String, StoreId>() {
            @Override
            public StoreId convert(String source) {
                return new StoreId(source);
            }
        });
        converters.add(new Converter<String, ProductId>() {
            @Override
            public ProductId convert(String source) {
                return new ProductId(source);
            }
        });
        converters.add(new Converter<String, ProductVariantId>() {
            @Override
            public ProductVariantId convert(String source) {
                return new ProductVariantId(source);
            }
        });
        converters.add(new Converter<String, Currency>() {
            @Override
            public Currency convert(String source) {
                return Currency.getInstance(source);
            }
        });

        converters.add(new Converter<String, ImageLink>() {
            @Override
            public ImageLink convert(String source) {
                return new ImageLink(source);
            }
        });
        converters.add(new Converter<String, CategoryId>() {
            @Override
            public CategoryId convert(String source) {
                return new CategoryId(source);
            }
        });


        return new JdbcCustomConversions(converters);
    }

    @Bean
    public <R extends Identifier, T extends BaseEntity<?, R>> AfterSaveCallback<T> afterSaveCallback() {
        return aggregate -> {
            aggregate.setNew(false);
            return aggregate;
        };
    }

}
