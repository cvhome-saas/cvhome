import {Fragment} from 'react';
import {Link} from '@store-front/i18n/navigation';
import type {BreadcrumbItem as Crumb} from '@store-front/types';
import {Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator} from '@store-front/ui/breadcrumb';

/** A trail of small strips. */
export function Breadcrumbs({items, className}: { items: Crumb[]; className?: string }) {
    if (items.length === 0) return null;
    const last = items[items.length - 1];
    return (
        <Breadcrumb className={className}>
            <BreadcrumbList className="gap-1.5 sm:gap-1.5">
                {items.slice(0, -1).map(c => (
                    <Fragment key={c.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild><Link prefetch={false} href={c.href} className="strip strip-hover h-7 px-2 text-xs">{c.name}</Link></BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="rtl:rotate-180 [&>svg]:size-3"/>
                    </Fragment>
                ))}
                <BreadcrumbItem><BreadcrumbPage className="strip h-7 bg-foreground px-2 text-xs text-background">{last.name}</BreadcrumbPage></BreadcrumbItem>
            </BreadcrumbList>
        </Breadcrumb>
    );
}
