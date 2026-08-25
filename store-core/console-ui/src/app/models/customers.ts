import type {OrderStatus} from '@models/checkout';

/**
 * The customers page's view models.
 *
 * The wire shapes live in `@models/checkout` — `ReadableCustomer` and `CustomerAddress` were ported
 * by the orders module and are reused here rather than declared a second time.
 *
 * **A customer is a thinner record than it looks.** `ReadableCustomer` is eight fields, and two of
 * them are addresses; there is no created-at, no status, no group, no tag, no note, no order tally
 * and no spend. Everything this file names is therefore either on that DTO or counted from the
 * orders endpoint — nothing is derived to fill a gap. See lessons.md, the Customers entries.
 */

/**
 * One address as the dialog draws it.
 *
 * A customer has exactly two slots, `billing` and `delivery`, and they are columns on the customer
 * row rather than an address book — so this is a shaped view of a fixed pair, not a list that can
 * grow. `lines` is pre-assembled because the parts that are present vary per record and an address
 * with an empty line in the middle of it reads as a rendering fault.
 */
export interface CustomerAddressView {
  readonly kind: 'billing' | 'delivery';
  readonly name: string;
  readonly company: string;
  readonly phone: string;
  readonly lines: readonly string[];
}

/**
 * One customer, as the table and the dialog read them.
 *
 * TODO(lessons.md): a customer's own name, and a created-at. See lessons.md, "Customers — a
 * customer's name comes from the billing address" and "Customers — no created-at on the DTO".
 *
 * `name` is empty for a customer who has never checked out with a billing address, and that is
 * shown as it is: `ReadableCustomerPopulator` copies the name off the billing address and never
 * maps the customer's own columns, so there is no other name to fall back to. Inventing one from
 * the email would put a value under a label the record does not hold. See lessons.md, "Customers —
 * a customer's name comes from the billing address".
 */
export interface CustomerRow {
  readonly id: number;
  readonly name: string;
  readonly email: string;
  readonly userName: string;
  readonly company: string;
  readonly phone: string;
  /** City and country, joined — the table's one location column. Empty when neither is known. */
  readonly location: string;
  /** The avatar monogram. Falls back to the email, because an avatar is not a name field. */
  readonly initials: string;
  readonly addresses: readonly CustomerAddressView[];
}

/** One of the customer's orders, in the dialog's activity panel. */
export interface CustomerOrderRow {
  readonly id: number;
  readonly status: OrderStatus | undefined;
  readonly datePurchased: string;
  /**
   * The amount and its currency, not a rendered string — the same shape `OrderRow` carries.
   *
   * The server's own `text` is null on every total the list returns, which QA caught here as an em
   * dash in place of every figure. `Money` formats from the numeric value and keeps the amount in
   * the language the operator is reading, rather than the browser's.
   */
  readonly total: {readonly value: number | null; readonly currency: string | null; readonly text: string | null};
}

/**
 * What the dialog's orders panel renders.
 *
 * `totalElements` is the customer's **exact** order count, read off the same response rather than
 * counted from `rows` — the panel shows the most recent handful and the count is all of them. It is
 * the one lifetime figure on this page that is real; the money ones are not, and say so.
 */
export interface CustomerOrdersSnapshot {
  readonly rows: readonly CustomerOrderRow[];
  readonly totalElements: number;
}
