package com.asrevo.cvhome.inventory.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistablePrice;
import com.asrevo.cvhome.inventory.model.PersistableSkuInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.http.ApiClient;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What the bulk upsert asks of the database: one read for the whole batch, however many skus it carries.
 *
 * <p>
 * It read each sku on its own, 21 statements for 20 skus in the 2026-09-14 load test. Same context as
 * {@code InventoryApiIntegrationTest}, so the two share one start-up.
 * </p>
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class InventoryServiceIntegrationTest {

    private static final StoreMerchantId STORE = new StoreMerchantId(Tokens.STORE_1);

    private static final int SKUS = 20;

    @Autowired
    private InventoryService inventory;

    @Test
    void aBulkUpsertReadsEveryExistingSkuInOneStatement() throws Exception {
        String prefix = ApiClient.slug("SKU-BULK-SQL");
        List<PersistableSkuInventory> created = batch(prefix, 1);
        inventory.bulkUpsert(STORE, created);

        SqlStatements.Recorded<List<SkuInventory>> edit = SqlStatements.during(
                () -> inventory.bulkUpsert(STORE, batch(prefix, 7)));

        assertThat(edit.result()).hasSize(SKUS).allSatisfy(row -> assertThat(row.quantity()).isEqualTo(7));
        // Every row's StockChanged and PriceChanged go through the outbox, whose JPA store reads before it writes;
        // those selects are the outbox's, not the upsert's, and the sku read is still one statement.
        assertThat(edit.statements().stream()
                .filter(sql -> sql.stripLeading().toLowerCase(Locale.ROOT).startsWith("select"))
                .filter(sql -> !sql.contains("outbox_record"))
                .count()).as(edit.toString()).isEqualTo(1);
    }

    private static List<PersistableSkuInventory> batch(String prefix, int quantity) {
        return IntStream.range(0, SKUS)
                .mapToObj(i -> new PersistableSkuInventory(String.format("%s-%d", prefix, i),
                        new PersistableInventory(null, quantity, true, null, null,
                                new PersistablePrice(new BigDecimal("20.00"), null, null, null))))
                .toList();
    }
}
