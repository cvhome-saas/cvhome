export interface BreadcrumbItem {
    id: string;
    name: string;
    href: string;
}

export interface BreadcrumbItems {
    prev: BreadcrumbItem[];
    current: BreadcrumbItem | undefined;
}