/**
 * One `Intl.NumberFormat` per locale and currency, shared by every render in the process.
 *
 * Building the formatter is the expensive part of formatting a price, and `InventoryService` used to build one for
 * every price of every product on every render: 3.9 % of a home render's CPU in a V8 profile of the storefront on
 * production's image. The formatter is stateless, so one instance serves concurrent renders. A code `Intl` rejects is
 * remembered as rejected, so a store with a bad currency costs one failed construction rather than one per price.
 */
const formatters = new Map<string, Intl.NumberFormat | null>();

/**
 * The keys are a store's languages times its currency, a handful. The locale comes from the request's route, so
 * the map is bounded rather than trusted: past this many entries it starts over.
 */
const MAX_FORMATTERS = 256;

/** The currency formatter for `locale` and `currency`, or `undefined` when `Intl` rejects either one. */
export function currencyFormatter(locale: string, currency: string): Intl.NumberFormat | undefined {
    const key = `${locale}|${currency}`;
    let formatter = formatters.get(key);
    if (formatter === undefined) {
        try {
            formatter = new Intl.NumberFormat(locale, {style: 'currency', currency});
        } catch {
            formatter = null;
        }
        if (formatters.size >= MAX_FORMATTERS) {
            formatters.clear();
        }
        formatters.set(key, formatter);
    }
    return formatter ?? undefined;
}
