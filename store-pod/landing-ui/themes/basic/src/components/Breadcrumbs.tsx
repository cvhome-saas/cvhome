import {Fragment} from 'react';
import {Link} from '@store-front/i18n/navigation';
import type {BreadcrumbItem as Crumb} from '@store-front/types';
import {Breadcrumb, BreadcrumbItem, BreadcrumbLink, BreadcrumbList, BreadcrumbPage, BreadcrumbSeparator} from '@store-front/ui/breadcrumb';

export function Breadcrumbs({items, className}: { items: Crumb[]; className?: string }) {
    if (items.length === 0) return null;
    const last = items[items.length - 1];
    return (
        <Breadcrumb className={className}>
            <BreadcrumbList>
                {items.slice(0, -1).map(c => (
                    <Fragment key={c.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild><Link prefetch={false} href={c.href}>{c.name}</Link></BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="rtl:rotate-180"/>
                    </Fragment>
                ))}
                <BreadcrumbItem><BreadcrumbPage>{last.name}</BreadcrumbPage></BreadcrumbItem>
            </BreadcrumbList>
        </Breadcrumb>
    );
}
