import {Description} from "@/types/description";

export interface CategoryPage {
    totalPages: number
    number: number
    recordsTotal: number
    recordsFiltered: number
    content: Category[] | undefined
}

export interface Category {
    id: number
    code: string
    sortOrder: number
    visible: boolean
    featured: boolean
    lineage: string
    depth: number
    description: Description
    productCount: number
    store: string
    parent: Category | undefined
    children: Category[] | undefined
}

