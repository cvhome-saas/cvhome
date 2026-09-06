/**
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

/**
 * `store-commons` `PaymentStatus`. The order carries it as a bare enum name, typed `string` on the
 * wire because checkout does not narrow it.
 */
export const PAYMENT_STATUSES: readonly string[] = [
  'PENDING',
  'PROCESSING',
  'PAID',
  'FAILED',
  'EXPIRED',
  'CANCELLED',
  'WAITING_VERIFICATION',
  'REJECTED',
  'AUTHORIZED',
  'REFUNDED',
];

/** `store-commons` `InventoryStatus`, which the order reports as `inventoryStatus`. */
export const INVENTORY_STATUSES: readonly string[] = [
  'AVAILABLE',
  'NOT_REQUESTED',
  'RESERVED',
  'COMMITTED',
  'RELEASED',
  'RESERVATION_FAILED',
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

/**
 * Mirrors checkout-commons `ReadableOrderProductAttribute` — one option/value label pair snapshotted
 * at placement ("Color" / "Red"). `attributePrice` is a Shopizer leftover the platform never fills.
 */
export interface ReadableOrderProductAttribute {
  readonly id?: number;
  readonly attributeName?: string;
  readonly attributeValue?: string;
  readonly attributePrice?: string;
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
  /**
   * The variant's option labels, copied into `order_product_option` rows at placement so the order
   * survives later catalog edits. `null` on the wire for a line with no options.
   */
  readonly attributes?: readonly ReadableOrderProductAttribute[] | null;
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
  /** The opaque ref checkout minted for this order — what payment holds as `requestRef`. */
  readonly orderRef?: string;
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
  readonly inventoryStatus?: string;
  readonly redirectUrl?: string;
  /** Set when automation could not finish the order (paid after cancel, commit refused); read `attentionReason`. */
  readonly needsAttention?: boolean;
  readonly attentionReason?: string;
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
 * The number inside a server-formatted amount, or null when there is none to find.
 *
 * Line items are the one place the order carries **no** raw value: `ReadableOrderProduct.price` and
 * `subTotal` are `String`, formatted by checkout, and nothing numeric sits behind them. Formatted in
 * whose locale is not the console's decision — the result is `SAR550.00` next to a total the console
 * renders as `٥٥٠٫٠٠ ر.س.`, two formats for the same currency on one screen.
 *
 * So the number is read back out and formatted with everything else. Grouping separators are
 * dropped and the last `.` or `,` is taken as the decimal point, which covers both the English form
 * the server emits today and a European one if it ever changes. Anything unparseable falls back to
 * the server's own string — see lessons.md, "Orders — line prices arrive formatted, with no number behind them".
 */
export function parseAmount(formatted: string | undefined): number | null {
  if (!formatted) {
    return null;
  }
  const digits = formatted.replace(/[^\d.,-]/g, '').trim();
  if (!digits) {
    return null;
  }
  const lastDot = digits.lastIndexOf('.');
  const lastComma = digits.lastIndexOf(',');
  const decimalAt = Math.max(lastDot, lastComma);
  // A separator with three digits behind it is grouping, not a decimal point: `1,234`.
  const isDecimal = decimalAt > -1 && digits.length - decimalAt - 1 !== 3;
  const normalized = isDecimal
    ? digits.slice(0, decimalAt).replace(/[.,]/g, '') + '.' + digits.slice(decimalAt + 1)
    : digits.replace(/[.,]/g, '');
  const value = Number(normalized);
  return Number.isFinite(value) ? value : null;
}

/**
 * The total modules checkout itself defines, lowercased — its `OrderTotalType` enum.
 *
 * A store's total modules are not limited to these: the field is a free string, which is why
 * anything outside the set is humanized rather than looked up. See `TotalLabel`.
 */
export const KNOWN_TOTAL_MODULES: ReadonlySet<string> = new Set([
  'shipping',
  'handling',
  'tax',
  'product',
  'subtotal',
  'total',
  'credit',
  'refund',
  'discount',
]);

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

