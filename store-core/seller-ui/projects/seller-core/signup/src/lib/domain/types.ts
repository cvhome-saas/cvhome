export interface SignUpForm {
  user: PersistableUser
  subscriptionPlan: string
}

export interface PersistableUser {
  firstName: string
  lastName: string
  emailAddress: string
  password: string
}

export interface SignUpResponse {
  status: string
}
