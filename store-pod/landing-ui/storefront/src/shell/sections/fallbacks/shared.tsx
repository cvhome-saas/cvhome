import type {ReactNode} from 'react';

/**
 * Shared bits of the shell's fallback renderers. Undesigned on purpose, like `default-search-page`: built only
 * from design tokens so they take on the active theme's type, colour, spacing and radius. A theme replaces a
 * kind by registering it in `sections`.
 */

export function SectionHeading({title, subtitle}: { title?: string; subtitle?: string }) {
    if (!title && !subtitle) return null;
    return (
        <header className="mb-5">
            {title && <h2 className="text-xl font-semibold"><bdi dir="auto">{title}</bdi></h2>}
            {subtitle && <p className="mt-1 text-sm text-muted-foreground"><bdi dir="auto">{subtitle}</bdi></p>}
        </header>
    );
}

/**
 * What an empty section renders: nothing on the live page, a labelled hint inside the builder's canvas, so a
 * merchant can still find and fill the block they just added.
 */
export function EmptyOrHint({preview, label}: { preview: boolean; label: string }) {
    if (!preview) return null;
    return (
        <div className="flex min-h-24 items-center justify-center rounded-md border border-dashed border-muted-foreground/40 p-6 text-sm text-muted-foreground">
            {label}
        </div>
    );
}

export function Section({children}: { children: ReactNode }) {
    return <div className="w-full">{children}</div>;
}
