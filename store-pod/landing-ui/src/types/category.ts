export interface CategoryPage {
    totalPages: number
    number: number
    recordsTotal: number
    recordsFiltered: number
    categories: Category[]
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
    parent: Category
    children: Category[]
}

export interface Description {
    id: number
    language: string
    name: string
    description: string
    friendlyUrl: string
    keyWords: any
    highlights: string
    metaDescription: string
    title: string
}
