/** Mirrors commons/customer address/AddressLocation -> customer/CustomerAddress */
export interface CustomerAddress {
  postalCode?: string;
  countryCode?: string;
  firstName?: string;
  lastName?: string;
  company?: string;
  phone?: string;
  address?: string;
  city?: string;
  stateProvince?: string;
  zone?: string;   // ZoneCode, custom-serialized to a plain string
  country?: string; // CountryIsoCode, custom-serialized to a plain string
}

/** Mirrors customer-commons model/customer/ReadableBilling -> BillingEntity -> CustomerAddress */
export interface ReadableBilling extends CustomerAddress {
  email?: string;
  countryName?: string;
  provinceName?: string;
}

/** Mirrors customer-commons model/customer/ReadableDelivery -> DeliveryEntity -> CustomerAddress */
export interface ReadableDelivery extends CustomerAddress {
  countryName?: string;
  provinceName?: string;
}

/** Mirrors customer-commons model/customer/ReadableCustomer -> CustomerEntity -> Customer -> Entity.
 *  NOTE: the Java field is `username`, not `userName` — a genuine mismatch
 *  found against order-details.facade.ts's original `data.customer.userName`
 *  read, fixed to match the real DTO rather than invented. */
export interface ReadableCustomer {
  id?: number;
  emailAddress?: string;
  billing?: CustomerAddress;
  delivery?: CustomerAddress;
  firstName?: string;
  lastName?: string;
  username?: string;
  cuaExternalId?: string;
}

/** Mirrors checkout-commons model/product/ReadableMinimalProduct (referenced by OrderProductEntity) */
export interface ReadableMinimalProduct {
  id?: number;
  sku?: string;
  name?: string;
}

/** Mirrors checkout-commons model/order/ReadableOrderProduct -> OrderProductEntity -> OrderProduct -> Entity */
export interface ReadableOrderProduct {
  id?: number;
  orderedQuantity?: number;
  product?: ReadableMinimalProduct;
  productName?: string;
  price?: string;
  subTotal?: string;
  sku?: string;
  image?: string;
}

/** Mirrors checkout-commons model/order/total/OrderTotal -> Entity */
export interface OrderTotal {
  id?: number;
  title?: string;
  text?: string;
  code?: string;
  order?: number;
  module?: string;
  value?: number;
}

/** Mirrors checkout-commons model/order/v0/ReadableOrder -> OrderEntity -> v0/Order -> Entity.
 *  `orderStatus` is a Java enum, serializes as its name (string). */
export interface ReadableOrder {
  id?: number;
  totals?: OrderTotal[];
  shippingModule?: string;
  previousOrderStatus?: string[];
  orderStatus?: string;
  datePurchased?: string;
  currency?: string;      // CurrencyCode, custom-serialized to a plain string
  customerAgreed?: boolean;
  confirmedAddress?: boolean;
  comments?: string;
  customer?: ReadableCustomer;
  products?: ReadableOrderProduct[];
  billing?: ReadableBilling;
  delivery?: ReadableDelivery;
  total?: OrderTotal;
  tax?: OrderTotal;
  shipping?: OrderTotal;
  paymentStatus?: string;
  reservationStatus?: string;
  redirectUri?: string;
}

/** Mirrors checkout-commons model/order/history/ReadableOrderStatusHistory -> OrderStatusHistory -> Entity */
export interface ReadableOrderStatusHistory {
  id?: number;
  orderId?: number;
  orderStatus?: string;
  comments?: string;
  date?: string;
}

/** Mirrors checkout-commons model/order/history/PersistableOrderStatusHistory -> OrderStatusHistory -> Entity */
export interface PersistableOrderStatusHistory {
  orderId?: number;
  orderStatus?: string;
  comments?: string;
  date?: string;
}

/** Mirrors reference-commons model/references/ReadableCountry -> CountryEntity -> Entity */
export interface ReadableCountry {
  id?: number;
  code?: string;    // CountryIsoCode, custom-serialized to a plain string
  supported?: boolean;
  name?: string;
  zones?: ReadableZone[];
}

/** Mirrors reference-commons model/references/ReadableZone -> ZoneEntity -> Entity */
export interface ReadableZone {
  id?: number;
  countryCode?: string; // CountryIsoCode, custom-serialized to a plain string
  code?: string;        // ZoneCode, custom-serialized to a plain string
  name?: string;
}

/** Local shape of the PATCH /private/orders/{id}/customer body — no matching
 *  controller was found in checkout-service for this endpoint (nor for
 *  /refund or /capture below); typed from what order-details.mapper.ts
 *  already constructs rather than a verified Java contract. */
export interface UpdateOrderPayload {
  emailAddress?: string;
  billing?: CustomerAddress & {billingAddress?: boolean};
  delivery?: CustomerAddress & {billingAddress?: boolean};
}
