import {Description} from "@/types/description";

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
    linkToMenu: boolean
    contentType: string
    description: Description
}

export interface Box {
    id: number
    code: string
    visible: boolean
    contentType: string
    description: Description
}
