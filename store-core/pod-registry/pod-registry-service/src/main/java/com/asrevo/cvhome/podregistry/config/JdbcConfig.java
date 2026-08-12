package com.asrevo.cvhome.podregistry.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Teaches Spring Data JDBC to read and write the identifier value objects stored as columns.
 *
 * <p>
 * One writing converter covers every {@link Identifier}; each type then needs its own reading converter, because the
 * target type selects it. A missing reading converter is not a compile error — it surfaces as a
 * {@code ConverterNotFoundException} the first time that column is queried.
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
        converters.add(new Converter<String, PodId>() {
            @Override
            public PodId convert(String source) {
                return new PodId(source);
            }
        });
        converters.add(new Converter<String, ManagerOrgId>() {
            @Override
            public ManagerOrgId convert(String source) {
                return new ManagerOrgId(source);
            }
        });
        // Reads pod_store_placement.store_id, which is that table's @Id and its repository's id type. Nothing
        // currently calls an inherited CrudRepository method on it — both callers go through custom @Query methods
        // taking Strings — so this is latent rather than live: it makes the declared id type actually usable, and
        // stops the first findById anyone adds from failing as the note above describes.
        converters.add(new Converter<String, StoreMerchantId>() {
            @Override
            public StoreMerchantId convert(String source) {
                return new StoreMerchantId(source);
            }
        });
        return converters;
    }

}
