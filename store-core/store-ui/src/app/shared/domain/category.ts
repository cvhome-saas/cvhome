export interface CategoryList {
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
  parent: Category
  description: Description
  productCount: number
  store: string
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
