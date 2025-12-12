import {Link} from "@/i18n/navigation";
import {BreadcrumbItems} from "@/types/bread-crumb";
import {
    Breadcrumb as ShadcnBreadcrumb,
    BreadcrumbItem,
    BreadcrumbLink,
    BreadcrumbList,
    BreadcrumbPage,
    BreadcrumbSeparator,
} from "@/components/ui/breadcrumb";
import React from "react";

export const Breadcrumb = ({breadcrumbs}: { breadcrumbs: BreadcrumbItems }) => {
    return (
        <ShadcnBreadcrumb className="mx-auto max-w-2xl px-4 sm:px-6 lg:max-w-7xl lg:px-8">
            <BreadcrumbList>
                {breadcrumbs.prev.map((breadcrumb) => (
                    <React.Fragment key={breadcrumb.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild>
                                <Link prefetch={false} href={breadcrumb.href}>
                                    {breadcrumb.name}
                                </Link>
                            </BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator/>
                    </React.Fragment>
                ))}
                {breadcrumbs.current && (
                    <BreadcrumbItem>
                        <BreadcrumbPage>{breadcrumbs.current.name}</BreadcrumbPage>
                    </BreadcrumbItem>
                )}
            </BreadcrumbList>
        </ShadcnBreadcrumb>
    );
};
