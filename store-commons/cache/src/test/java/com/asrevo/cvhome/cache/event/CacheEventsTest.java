package com.asrevo.cvhome.cache.event;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.commons.domain.CategoryId;
import com.asrevo.cvhome.commons.domain.ManufacturerId;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.VariantId;

import io.namastack.outbox.annotation.OutboxEvent;

import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Every event names its store, partitions on it, is typed by its class name, carries its ids as plain strings for
 * the log and survives the outbox's JSON round trip with its typed ids intact.
 */
class CacheEventsTest {

    private static final long SEVEN = 7L;

    private static final String SKU = "SKU-1";

    private static final String STORE = "store";

    private static final String PAGE = "page";

    @Test
    void everyEventIsKeyedByItsStoreAndTypedByItsClass() {
        Sku sku = new Sku(SKU);
        List<CacheEvent> events = List.of(new ProductChanged(Stores.A, new ProductId(SEVEN)),
                new VariantChanged(Stores.A, new VariantId(SEVEN)), new CategoryChanged(Stores.A, new CategoryId(SEVEN)),
                new ManufacturerChanged(Stores.A, new ManufacturerId(SEVEN)), new StockChanged(Stores.A, sku),
                new PriceChanged(Stores.A, sku), new StoreChanged(Stores.A), new ContentChanged(Stores.A, PAGE));

        for (CacheEvent event : events) {
            assertThat(event.store()).isEqualTo(Stores.A);
            assertThat(event.partitionKey()).isEqualTo(Stores.A.getId());
            assertThat(event.eventType()).isEqualTo(event.getClass().getSimpleName());
            assertThat(event.data()).containsEntry(STORE, Stores.A.getId());
            assertThat(event.getClass().getAnnotation(OutboxEvent.class).key()).isEqualTo("#this.partitionKey()");
        }
        assertThat(new ProductChanged(Stores.A, new ProductId(SEVEN)).data()).containsEntry("productId", "7");
        assertThat(new StockChanged(Stores.A, sku).data()).containsEntry("sku", SKU);
        assertThat(new ContentChanged(Stores.A, PAGE).data()).containsEntry("kind", PAGE);
    }

    @Test
    void anEventWithoutItsStoreOrSubjectIsRefused() {
        assertThatThrownBy(() -> new ProductChanged(null, new ProductId(SEVEN)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StockChanged(Stores.A, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StoreChanged(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ContentChanged(Stores.A, " ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anEventSurvivesTheOutboxJsonRoundTripWithItsTypedIds() {
        JsonMapper mapper = JsonMapper.builder().build();
        ProductChanged product = new ProductChanged(Stores.A, new ProductId(SEVEN));
        StockChanged stock = new StockChanged(Stores.B, new Sku(SKU));

        String productJson = mapper.writeValueAsString(product);
        String stockJson = mapper.writeValueAsString(stock);

        assertThat(productJson).contains(String.format("\"store\":\"%s\"", Stores.A.getId())).contains("\"product\":7")
                .doesNotContain("partitionKey");
        assertThat(mapper.readValue(productJson, ProductChanged.class)).isEqualTo(product);
        assertThat(mapper.readValue(stockJson, StockChanged.class)).isEqualTo(stock);
    }
}
