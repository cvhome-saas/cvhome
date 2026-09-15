package com.asrevo.cvhome.cache.event;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.asrevo.cvhome.cache.Stores;
import com.asrevo.cvhome.commons.domain.CategoryId;
import com.asrevo.cvhome.commons.domain.ManufacturerId;
import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.VariantId;

import io.namastack.outbox.annotation.OutboxHandler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

/** Every event type has a handler method; each applies locally first and then publishes. */
class CacheEventOutboxHandlerTest {

    private final CacheEventApplier applier = mock(CacheEventApplier.class);

    private final CacheEventTransport transport = mock(CacheEventTransport.class);

    private final CacheEventOutboxHandler handler = new CacheEventOutboxHandler(applier, transport);

    @Test
    void everyEventIsAppliedLocallyAndThenPublished() {
        Sku sku = new Sku("SKU-1");
        List<CacheEvent> events = List.of(new ProductChanged(Stores.A, new ProductId(1L)),
                new VariantChanged(Stores.A, new VariantId(1L)), new CategoryChanged(Stores.A, new CategoryId(1L)),
                new ManufacturerChanged(Stores.A, new ManufacturerId(1L)), new StockChanged(Stores.A, sku),
                new PriceChanged(Stores.A, sku), new StoreChanged(Stores.A), new ContentChanged(Stores.A, "page"));

        handler.onProductChanged((ProductChanged) events.get(0));
        handler.onVariantChanged((VariantChanged) events.get(1));
        handler.onCategoryChanged((CategoryChanged) events.get(2));
        handler.onManufacturerChanged((ManufacturerChanged) events.get(3));
        handler.onStockChanged((StockChanged) events.get(4));
        handler.onPriceChanged((PriceChanged) events.get(5));
        handler.onStoreChanged((StoreChanged) events.get(6));
        handler.onContentChanged((ContentChanged) events.get(7));

        InOrder order = inOrder(applier, transport);
        for (CacheEvent event : events) {
            order.verify(applier).apply(event);
            order.verify(transport).publish(event);
        }
        order.verifyNoMoreInteractions();
    }

    @Test
    void everyPermittedEventTypeHasAnOutboxHandlerMethod() {
        List<Class<?>> handled = new ArrayList<>();
        for (Method method : CacheEventOutboxHandler.class.getMethods()) {
            if (method.isAnnotationPresent(OutboxHandler.class)) {
                handled.add(method.getParameterTypes()[0]);
            }
        }

        assertThat(handled).containsExactlyInAnyOrder(CacheEvent.class.getPermittedSubclasses());
    }
}
