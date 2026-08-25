package com.asrevo.cvhome.inventory.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.sql.DataSource;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Copies pre-split availability/price/reservation data out of the catalog schema on startup.
 *
 * <p>
 * Not part of {@code spring.sql.init}: the script is one Postgres {@code DO $$ ... $$} block, whose inner semicolons
 * Spring's script splitter would cut apart. Executed here as a single statement instead. The block itself is
 * idempotent and guards on the catalog tables still existing, so running it on every startup is safe — and a no-op
 * on a fresh environment or after the old tables are dropped.
 * </p>
 */
@Component
@Slf4j
public class CatalogDataMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public CatalogDataMigration(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        String sql = new ClassPathResource("init-sql/migrate-from-catalog.sql")
                .getContentAsString(StandardCharsets.UTF_8);
        jdbcTemplate.execute(sql);

        Integer nullSkus = jdbcTemplate.queryForObject(
                "select count(*) from inventory.product_availability where sku is null", Integer.class);
        if (nullSkus != null && nullSkus > 0) {
            // The sku is the reservation path's only key — a row without one is unreservable and unsellable.
            log.error("{} inventory.product_availability rows have no sku after migration; unreservable by sku.",
                    nullSkus);
        } else {
            log.info("Catalog-to-inventory data migration completed; all availability rows carry a sku.");
        }
    }

}
