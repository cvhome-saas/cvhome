package com.asrevo.cvhome.inventory.reads;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.config.ExternalClientsTestConfiguration;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.repositories.InventoryRepository;
import com.asrevo.cvhome.testsupport.annotations.ServiceIntegrationTest;
import com.asrevo.cvhome.testsupport.security.Tokens;
import com.asrevo.cvhome.testsupport.sql.SqlStatements;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A sku's stock on a real inventory: the second read costs no statement, and a committed change to the row (what
 * a reservation does) makes the next read of that store pay and show the new figure, while the other store's entry
 * stays warm.
 */
@ServiceIntegrationTest
@Import(ExternalClientsTestConfiguration.class)
class SkuInventoryReadsIntegrationTest {

    private static final StoreMerchantId STORE_A = new StoreMerchantId(Tokens.STORE_1);

    private static final StoreMerchantId STORE_B = new StoreMerchantId(Tokens.STORE_2);

    private static final Sku SEEDED = Sku.of("SKU-NK-RUN-001");

    private static final Sku OTHER_STORES = Sku.of("ELEC-SKU-136");

    @Autowired
    private SkuInventoryReads reads;

    @Autowired
    private InventoryRepository inventories;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void aSecondReadCostsNoStatementAndACommittedStockChangeIsSeenAtOnce() throws Exception {
        List<SkuInventory> first = reads.bySkus(STORE_A, List.of(SEEDED));
        reads.bySkus(STORE_B, List.of(OTHER_STORES));
        assertThat(first).hasSize(1);
        int quantity = first.getFirst().quantity();

        SqlStatements.Recorded<List<SkuInventory>> again = SqlStatements.during(() -> reads.bySkus(STORE_A, List.of(SEEDED)));
        assertThat(again.count()).as(again.toString()).isZero();

        transactions.executeWithoutResult(status -> {
            Inventory row = inventories.findBySkus(STORE_A, List.of(SEEDED)).getFirst();
            row.setQuantity(row.getQuantity() + 1);
            inventories.save(row);
        });

        SqlStatements.Recorded<List<SkuInventory>> afterWrite = SqlStatements.during(() -> reads.bySkus(STORE_A, List.of(SEEDED)));
        assertThat(afterWrite.count()).as("the store whose row changed reads the database again").isPositive();
        assertThat(afterWrite.result().getFirst().quantity()).isEqualTo(quantity + 1);
        SqlStatements.Recorded<List<SkuInventory>> other = SqlStatements.during(() -> reads.bySkus(STORE_B, List.of(OTHER_STORES)));
        assertThat(other.count()).as("the other store's entry stays warm").isZero();
    }
}
