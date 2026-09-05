# checkout-deprecated — the previous checkout service (reference only)

Retired 2026-09 by the checkout rewrite (`.agents/plans/checkout-rewrite.md`). **Not registered in
`settings.gradle`; nothing may depend on it.** It is kept for one release as archaeology, then deleted.

Why it was retired: order placement ran save → remote reserve → status write → remote payment → status write
with no recovery, so a crash mid-way lost the order; its own `schema.sql` refused five of the ten payment
statuses the code wrote; nothing guarded status transitions; the payment/inventory callback had no permission
gate; and the customer and country entities lived in shared modules under this service's package.

What replaced it (`store-pod/checkout/`):

| Old | New |
|---|---|
| `OrderPlacementFacadeImpl` + `OrderInventoryOrchestratorImpl` | `services/order/OrderPlacementServiceImpl` + `OrderStepRunner` (durable pending actions, `@Version`) |
| `OrderFacadeImpl` / `OrderServiceImpl` / populators / mappers | `entity/Order` (transitions as methods) + `services/order/{OrderServiceImpl, OrderMapper}` |
| `ExternalOrderApi` (`POST /private/order/{ref}/payment-status`) | `api/v1/order/ExternalOrderSignalApi` (`POST /private/orders/{orderRef}/signals/*`, `STORE-POD.CHECKOUT.SIGNAL`) |
| `OrderTimeoutService` | `services/jobs/OrderExpiryJob` + `OrderRecoveryJob` |
| `ShoppingCartFacadeImpl` / `ShoppingCartServiceImpl` | `services/cart/CartServiceImpl` |
| `commons/customer-core` (`CustomerFacadeImpl`, `Customer` entity) | `entity/Customer` + `services/customer/CustomerServiceImpl` |
| `commons/reference-core` (country/zone/currency/language tables) | `services/reference/CountryServiceImpl` (JDK `Locale` ISO list, no tables) |
| `OrderStatisticApi` / `CustomerStatisticApi` / `ProductStatisticApi` | `api/v2/statistic/StatisticApi` over `OrderStatisticsService` |
| tables `orders`, `order_*`, `shopping_cart*`, `customer*`, `country*`, `zone*`, … | `sales_order*`, `cart*`, `customer_account`; the legacy tables are dropped once by `init-sql/drop-legacy.sql` |

`docs/` holds the three flow documents that described this implementation. They describe **this** code, not the
new service.

`init-sql/drop-legacy.sql` in the new service can be deleted once every environment has booted the new service
at least once.
