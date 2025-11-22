export interface Org {
  id: Id,
  owner: Id,
  createdDate: string,
  email: Email,
  subscriptionPlan: string
}

interface Id {
  id: string
}

interface Email {
  email: string
}
