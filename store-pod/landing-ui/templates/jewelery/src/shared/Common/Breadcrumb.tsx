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
            <BreadcrumbList className="text-[10px] tracking-widest uppercase">
                {breadcrumbs.prev.map((breadcrumb) => (
                    <React.Fragment key={breadcrumb.id}>
                        <BreadcrumbItem>
                            <BreadcrumbLink asChild>
                                <Link
                                    prefetch={false}
                                    href={breadcrumb.href}
                                    className="text-muted-foreground hover:text-primary transition-colors duration-200"
                                >
                                    {breadcrumb.name}
                                </Link>
                            </BreadcrumbLink>
                        </BreadcrumbItem>
                        <BreadcrumbSeparator className="text-border">
                            <span className="text-primary/50">·</span>
                        </BreadcrumbSeparator>
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
