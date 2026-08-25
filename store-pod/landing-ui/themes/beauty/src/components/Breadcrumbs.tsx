import {Fragment} from 'react';
import {Link} from '@store-front/i18n/navigation';
import type {BreadcrumbItem as Crumb} from '@store-front/types';
import {Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage} from '@store-front/ui/breadcrumb';

/** Zone plates: HOME / MEN / JACKET — separated by a slash, the last one struck in ink. */
export function Breadcrumbs({items, className}: { items: Crumb[]; className?: string }) {
    if (items.length === 0) return null;
    const last = items[items.length - 1];
    return (
        <Breadcrumb className={className}>
            <BreadcrumbList className="gap-0 font-mono text-xs uppercase tracking-wide sm:gap-0">
                {items.slice(0, -1).map(c => (
                    <Fragment key={c.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild><Link prefetch={false} href={c.href} className="plate px-2 py-1 hover:bg-foreground hover:text-background"><span className="q">{c.name}</span></Link></BreadcrumbLink>
                        </BreadcrumbItem>
                        <li aria-hidden className="px-1.5 text-muted-foreground">/</li>
                    </Fragment>
                ))}
                <BreadcrumbItem><BreadcrumbPage className="q bg-foreground px-2 py-1 text-background">{last.name}</BreadcrumbPage></BreadcrumbItem>
            </BreadcrumbList>
        </Breadcrumb>
    );
}
