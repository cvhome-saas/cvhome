import type {ReactNode} from 'react';

/**
 * Shared bits of the shell's fallback renderers. Undesigned on purpose, like `default-search-page`: built only
 * from design tokens so they take on the active theme's type, colour, spacing and radius. A theme replaces a
 * kind by registering it in `sections`.
 */

export {EmptyOrHint} from '@store-front/ui/sections/compose';

export function SectionHeading({title, subtitle, meta}: {title?: ReactNode; subtitle?: ReactNode; meta?: ReactNode}) {
    if (!title && !subtitle) return null;
    return (
        <header className="mb-5">
            <div className="flex flex-wrap items-baseline gap-x-3">
                {title && <h2 className="text-xl font-semibold">{title}</h2>}
                {meta !== undefined && <span className="text-sm text-muted-foreground">{meta}</span>}
            </div>
            {subtitle && <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>}
        </header>
    );
}

export function Section({children}: {children: ReactNode}) {
    return <div className="w-full">{children}</div>;
}
