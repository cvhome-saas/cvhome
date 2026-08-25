package com.asrevo.cvhome.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.inventory.config.CatalogDataMigration;
import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The context, the schema, the seed, and the startup migration's two outcomes.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class InventoryContextIntegrationTest {

    private static final String COUNT_NULL_SKUS =
            "select count(*) from inventory.product_availability where sku is null";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CatalogDataMigration migration;

    @Test
    void initializesOnlyInventorySchema() {
        var tables = jdbcTemplate.queryForList("""
                select table_name from information_schema.tables where table_schema = 'inventory' order by table_name
                """, String.class);
        assertThat(tables).contains("product_availability", "product_price", "product_reservation",
                "product_reservation_line", "sm_sequencer");
        Integer catalogSchemas = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.schemata where schema_name = 'catalog'", Integer.class);
        assertThat(catalogSchemas).isZero();
    }

    @Test
    void seedsEveryTestStoreWithStockAndAPrice() {
        Integer stores = jdbcTemplate.queryForObject(
                "select count(distinct store_merchant_id) from inventory.product_availability", Integer.class);
        assertThat(stores).isEqualTo(4);
        Integer unpriced = jdbcTemplate.queryForObject("""
                select count(*) from inventory.product_availability a
                 where not exists (select 1 from inventory.product_price p where p.product_avail_id = a.product_avail_id)
                """, Integer.class);
        assertThat(unpriced).isZero();
    }

    @Test
    void migrationIsIdempotentAndReportsRowsWithoutASku() throws Exception {
        assertThat(jdbcTemplate.queryForObject(COUNT_NULL_SKUS, Integer.class)).isZero();
        migration.run(null);
        assertThat(jdbcTemplate.queryForObject(COUNT_NULL_SKUS, Integer.class)).isZero();

        jdbcTemplate.update("""
                insert into inventory.product_availability (product_avail_id, store_merchant_id, quantity, sku)
                values (999001, '65f023632bc46470c104b76f', 1, null)
                """);
        try {
            migration.run(null);
            assertThat(jdbcTemplate.queryForObject(COUNT_NULL_SKUS, Integer.class)).isOne();
        } finally {
            jdbcTemplate.update("delete from inventory.product_availability where product_avail_id = 999001");
        }
    }
}
