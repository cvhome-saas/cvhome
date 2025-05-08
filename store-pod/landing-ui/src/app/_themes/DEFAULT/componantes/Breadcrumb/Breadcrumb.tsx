import {Link} from "@/i18n/navigation";
import {BreadcrumbItems} from "@/types/bread-crumb";

export const Breadcrumb = ({breadcrumbs}: { breadcrumbs: BreadcrumbItems }) => {
    return (
        <nav aria-label="Breadcrumb">
            <ol role="list"
                className="mx-auto flex max-w-2xl items-center space-x-2 px-4 sm:px-6 lg:max-w-7xl lg:px-8">
                {breadcrumbs.prev.map((breadcrumb) => (
                    <li key={breadcrumb.id}>
                        <div className="flex items-center">
                            <Link prefetch={false} href={breadcrumb.href}
                                  className="me-2 text-sm font-medium text-foreground">
                                {breadcrumb.name}
                            </Link>
                            <svg
                                fill="currentColor"
                                width={16}
                                height={20}
                                viewBox="0 0 16 20"
                                aria-hidden="true"
                                className="h-5 w-4 text-neutral rtl:-scale-x-100"
                            >
                                <path d="M5.697 4.34L8.98 16.532h1.327L7.025 4.341H5.697z"/>
                            </svg>
                        </div>
                    </li>
                ))}
                {breadcrumbs.current &&
                    <li className="text-sm">
                        <Link prefetch={false} href={breadcrumbs.current.href} aria-current="page"
                              className="font-medium text-neutral hover:text-hover-neutral">
                            {breadcrumbs.current.name}
                        </Link>
                    </li>
                }
            </ol>
        </nav>
    )
}
