/**
 * Ported from seller-ui/projects/seller-core/orders/src/lib/models/order.model.ts.
 *
 * Checkout's order shapes. Almost everything is optional because the Java DTOs populate what the
 * order actually has: a digital order carries no delivery address, an unpaid one carries no payment
 * status. That optionality is real, not defensive typing, and the screens narrow rather than assert.
 */

/** The ten values of `store-commons` `OrderStatus`. The server sends the enum name. */
export type OrderStatus =
  | 'CREATED'
  | 'PENDING_PAYMENT'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERING'
  | 'DELIVERED'
  | 'COMPLETED'
  | 'CANCELLED'
  | 'RETURNED';

/** In the order a fulfilment actually proceeds, which is the order the filter strip lists them in. */
export const ORDER_STATUSES: readonly OrderStatus[] = [
  'CREATED',
  'PENDING_PAYMENT',
  'CONFIRMED',
  'PROCESSING',
  'SHIPPED',
  'DELIVERING',
  'DELIVERED',
  'COMPLETED',
  'CANCELLED',
  'RETURNED',
];

/** Everything before the parcel moves — what "awaiting fulfilment" counts, on this page and the dashboard. */
export const AWAITING_FULFILMENT: readonly OrderStatus[] = [
  'CREATED',
  'PENDING_PAYMENT',
  'CONFIRMED',
  'PROCESSING',
];

/** Mirrors commons customer address `AddressLocation` → `CustomerAddress`. */
export interface CustomerAddress {
  readonly postalCode?: string;
  readonly countryCode?: string;
  readonly firstName?: string;
  readonly lastName?: string;
  readonly company?: string;
  readonly phone?: string;
  readonly address?: string;
  readonly city?: string;
  readonly stateProvince?: string;
  /** `ZoneCode`, custom-serialized to a plain string. */
  readonly zone?: string;
  /** `CountryIsoCode`, custom-serialized to a plain string. */
  readonly country?: string;
}

/** Mirrors customer-commons `ReadableBilling` → `BillingEntity` → `CustomerAddress`. */
export interface ReadableBilling extends CustomerAddress {
  readonly email?: string;
  readonly countryName?: string;
  readonly provinceName?: string;
}

/** Mirrors customer-commons `ReadableDelivery`. */
export interface ReadableDelivery extends CustomerAddress {
  readonly countryName?: string;
  readonly provinceName?: string;
}

/**
 * Mirrors customer-commons `ReadableCustomer`.
 *
 * The Java field is `username`, not `userName` — a genuine mismatch seller-core found against the
 * original call site and fixed to match the DTO rather than the guess. Kept.
 */
export interface ReadableCustomer {
  readonly id?: number;
  readonly emailAddress?: string;
  readonly billing?: CustomerAddress;
  readonly delivery?: CustomerAddress;
  readonly firstName?: string;
  readonly lastName?: string;
  readonly username?: string;
  readonly cuaExternalId?: string;
}

export interface ReadableMinimalProduct {
  readonly id?: number;
  readonly sku?: string;
  readonly name?: string;
}

/** Mirrors checkout-commons `ReadableOrderProduct`. `price` and `subTotal` arrive pre-formatted. */
export interface ReadableOrderProduct {
  readonly id?: number;
  readonly orderedQuantity?: number;
  readonly product?: ReadableMinimalProduct;
  readonly productName?: string;
  readonly price?: string;
  readonly subTotal?: string;
  readonly sku?: string;
  readonly image?: string;
}

/**
 * Mirrors checkout-commons `OrderTotal`.
 *
 * `text` is the money already formatted by the server in the order's currency; `value` is the raw
 * number. The screens render `text` — the server knows the currency and the console does not have to
 * re-derive a format from it.
 */
export interface OrderTotal {
  readonly id?: number;
  readonly title?: string;
  readonly text?: string;
  readonly code?: string;
  readonly order?: number;
  readonly module?: string;
  readonly value?: number;
}

/** Mirrors checkout-commons `ReadableOrder`. `orderStatus` serializes as the enum name. */
export interface ReadableOrder {
  readonly id?: number;
  readonly totals?: readonly OrderTotal[];
  readonly shippingModule?: string;
  readonly previousOrderStatus?: readonly string[];
  readonly orderStatus?: OrderStatus;
  readonly datePurchased?: string;
  /** `CurrencyCode`, custom-serialized to a plain string. */
  readonly currency?: string;
  readonly customerAgreed?: boolean;
  readonly confirmedAddress?: boolean;
  readonly comments?: string;
  readonly customer?: ReadableCustomer;
  readonly products?: readonly ReadableOrderProduct[];
  readonly billing?: ReadableBilling;
  readonly delivery?: ReadableDelivery;
  readonly total?: OrderTotal;
  readonly tax?: OrderTotal;
  readonly shipping?: OrderTotal;
  readonly paymentStatus?: string;
  readonly reservationStatus?: string;
  readonly redirectUri?: string;
}

export interface ReadableOrderStatusHistory {
  readonly id?: number;
  readonly orderId?: number;
  readonly orderStatus?: OrderStatus;
  readonly comments?: string;
  readonly date?: string;
}

export interface PersistableOrderStatusHistory {
  readonly orderId?: number;
  readonly orderStatus: OrderStatus;
  readonly comments?: string;
  readonly date?: string;
}

/**
 * An amount, as a person reads it.
 *
 * `OrderTotal.text` is where the server *may* put a formatted string, and against the running stack
 * it is **null on every total, on both the list and the detail endpoint** — only `products[].price`
 * and `subTotal` arrive pre-formatted. So `text` is preferred when present and the raw `BigDecimal`
 * `value` is formatted here otherwise. `value` is a decimal amount, not minor units.
 */
export function formatMoney(total: OrderTotal | undefined, currency: string | undefined): string {
  if (total?.text) {
    return total.text;
  }
  if (total?.value === undefined || total.value === null) {
    return '—';
  }
  if (!currency) {
    return String(total.value);
  }
  try {
    return new Intl.NumberFormat(undefined, {style: 'currency', currency}).format(total.value);
  } catch {
    // An unknown ISO code would otherwise throw and take the page with it.
    return `${currency} ${total.value}`;
  }
}

/**
 * A total's label.
 *
 * `title` is null on every total the server sends, so the line is named from `module` — `subtotal`,
 * `total`, `tax`, `shipping` and whatever else a store's total modules add. Humanized rather than
 * translated, for the same reason statuses are: the set is the server's, not the console's.
 */
export function totalLabel(total: OrderTotal): string {
  const name = total.title ?? total.module ?? total.code?.split('.').pop() ?? '';
  return name
    .replace(/[_-]+/g, ' ')
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/^./, (c) => c.toUpperCase());
}

/** True when an address object is present but every field on it is empty. */
export function isEmptyAddress(address: CustomerAddress | undefined): boolean {
  if (!address) {
    return true;
  }
  return !Object.values(address).some((value) => typeof value === 'string' && value.trim() !== '');
}

export interface ReadableCountry {
  readonly id?: number;
  readonly code?: string;
  readonly name?: string;
  readonly supported?: boolean;
}

export interface ReadableZone {
  readonly id?: number;
  readonly code?: string;
  readonly name?: string;
  readonly countryCode?: string;
}
