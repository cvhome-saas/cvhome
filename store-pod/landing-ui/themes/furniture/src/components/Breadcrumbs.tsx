import {Fragment} from 'react';
import {Link} from '@store-front/i18n/navigation';
import type {BreadcrumbItem as Crumb} from '@store-front/types';
import {Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator} from '@store-front/ui/breadcrumb';
import {cn} from '@store-front/ui/lib/utils';

/** The route through the building, lettered like the signs it passes. */
export function Breadcrumbs({items, className}: { items: Crumb[]; className?: string }) {
    if (items.length === 0) return null;
    const last = items[items.length - 1];
    return (
        <Breadcrumb className={cn('sign text-[0.625rem]', className)}>
            <BreadcrumbList className="gap-1.5 text-[0.625rem] sm:gap-2">
                {items.slice(0, -1).map(c => (
                    <Fragment key={c.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild><Link prefetch={false} href={c.href}>{c.name}</Link></BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="rtl:rotate-180 [&>svg]:size-3"/>
                    </Fragment>
                ))}
                <BreadcrumbItem><BreadcrumbPage className="text-foreground">{last.name}</BreadcrumbPage></BreadcrumbItem>
            </BreadcrumbList>
        </Breadcrumb>
    );
}
