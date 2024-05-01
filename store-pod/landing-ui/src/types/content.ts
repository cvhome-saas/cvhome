export interface ContentPage {
    totalPages: number
    number: number
    recordsTotal: number
    recordsFiltered: number
    items: Page[]
}

export interface Page {
    id: number
    code: string
    visible: boolean
    contentType: string
    description: Description
}

export interface Description {
    id: number
    language: string
    name: string
    description: string
    friendlyUrl: string
    keyWords: any
    highlights: any
    metaDescription: any
    title: string
}
