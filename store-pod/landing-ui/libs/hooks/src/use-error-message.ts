'use client'
import {useCallback} from "react";
import {useTranslations} from "next-intl";
import {ErrorParams, isApiError} from "@store-front/types";

/**
 * `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE` → `CODE.CHECKOUT_CART_QUANTITY_OUT_OF_RANGE`. The dots are the
 * backend's namespacing; next-intl reads a dot as a path separator, so the key flattens to underscores.
 */
const codeKey = (code: string): string => `CODE.${code.replace(/\./g, '_')}`;

/** Only what ICU can interpolate. A nested object in `params` would throw inside the formatter. */
function interpolatable(params: ErrorParams): Record<string, string | number> {
    const values: Record<string, string | number> = {};
    for (const [name, value] of Object.entries(params)) {
        if (typeof value === 'string' || typeof value === 'number') {
            values[name] = value;
        } else if (typeof value === 'boolean') {
            values[name] = String(value);
        }
    }
    return values;
}

/**
 * The shopper-facing message for a failed call.
 *
 * Every service answers with the same extended RFC-7807 body, and `locales/*.json` has carried a message
 * per `code` since the error contract landed — but nothing read them: every interactive failure notified
 * one fixed string per action, so "you can order at most 1 of this" and "we are offline" both surfaced as
 * "Failed to add product to cart." The catalogue was dead copy, and shoppers were told nothing they could
 * act on.
 *
 * The chain is code → the caller's own fallback → category → generic. The caller's fallback outranks the
 * category because it names the action ("Failed to add product to cart") where the category can only
 * restate the status ("We couldn't complete that request"); the client-side codes a shopper can actually
 * act on, `CLIENT.NETWORK_UNAVAILABLE` above all, resolve at the first step and never reach it.
 */
export function useErrorMessage() {
    const t = useTranslations('ERRORS');
    return useCallback((error: unknown, fallback?: string): string => {
        if (isApiError(error)) {
            const key = codeKey(error.code);
            if (t.has(key)) {
                try {
                    return t(key, interpolatable(error.params ?? {}));
                } catch {
                    // A message whose placeholders this refusal did not carry — one code, two shapes.
                    // Fall through rather than render a half-formatted string.
                }
            }
            if (fallback) {
                return fallback;
            }
            const category = `CATEGORY.${error.category}`;
            return t.has(category) ? t(category) : t('GENERIC');
        }
        return fallback ?? t('GENERIC');
    }, [t]);
}
