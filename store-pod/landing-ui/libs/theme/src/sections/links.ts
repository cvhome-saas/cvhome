/** A `link` prop as the builder stores it. */
export interface SectionLink {
    type?: 'url' | 'category' | 'product';
    value?: string;
}

/**
 * Locale-relative href for a stored link; `#` when it points nowhere. Shared with themes so their
 * section renderers resolve the field DSL's `link` values the same way the shell fallbacks do.
 */
export function linkHref(raw: unknown): string {
    const link = (raw ?? {}) as SectionLink;
    if (!link.value) return '#';
    switch (link.type) {
        case 'category':
            return `/category/${link.value}`;
        case 'product':
            return `/product/${link.value}`;
        default:
            return link.value;
    }
}
