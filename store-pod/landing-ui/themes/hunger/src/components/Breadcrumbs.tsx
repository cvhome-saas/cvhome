import {Fragment} from 'react';
import {Link} from '@store-front/i18n/navigation';
import type {BreadcrumbItem as Crumb} from '@store-front/types';
import {cn} from '@store-front/ui/lib/utils';

/** The reading path, printed as the sheet's small print: SECTION / SUBSECTION / DISH. */
export function Breadcrumbs({items, className}: { items: Crumb[]; className?: string }) {
    if (items.length === 0) return null;
    const last = items[items.length - 1];
    return (
        <nav aria-label="breadcrumb" className={cn('press flex flex-wrap items-center gap-x-2 text-xs tracking-wide text-muted-foreground', className)}>
            {items.slice(0, -1).map(c => (
                <Fragment key={c.id}>
                    <Link prefetch={false} href={c.href} className="hover:text-foreground hover:underline">{c.name}</Link>
                    <span aria-hidden className="text-border">/</span>
                </Fragment>
            ))}
            <span aria-current="page" className="text-foreground">{last.name}</span>
        </nav>
    );
}
