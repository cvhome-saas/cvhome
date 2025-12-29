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
        <ShadcnBreadcrumb className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
            <BreadcrumbList className="text-xs tracking-[0.18em] uppercase text-muted-foreground">
                {breadcrumbs.prev.map((breadcrumb) => (
                    <React.Fragment key={breadcrumb.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild>
                                <Link
                                    prefetch={false}
                                    href={breadcrumb.href}
                                    className="transition-colors hover:text-foreground"
                                >
                                    {breadcrumb.name}
                                </Link>
                            </BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="opacity-40"/>
                    </React.Fragment>
                ))}

                {breadcrumbs.current && (
                    <BreadcrumbItem>
                        <BreadcrumbPage className="text-foreground font-medium">
                            {breadcrumbs.current.name}
                        </BreadcrumbPage>
                    </BreadcrumbItem>
                )}
            </BreadcrumbList>
        </ShadcnBreadcrumb>
    );
};