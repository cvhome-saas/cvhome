package com.asrevo.cvhome.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The context, the schema in its final shape, and the seed.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class InventoryContextIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void skuIsMandatoryAndUniquePerStore() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into inventory.product_availability (product_avail_id, store_merchant_id, quantity, sku)
                values (999001, '65f023632bc46470c104b76f', 1, null)
                """)).isInstanceOf(DataIntegrityViolationException.class);

        String existingSku = jdbcTemplate.queryForObject("""
                select sku from inventory.product_availability
                 where store_merchant_id = '65f023632bc46470c104b76f' order by product_avail_id limit 1
                """, String.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                insert into inventory.product_availability (product_avail_id, store_merchant_id, quantity, sku)
                values (999002, '65f023632bc46470c104b76f', 1, ?)
                """, existingSku)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
