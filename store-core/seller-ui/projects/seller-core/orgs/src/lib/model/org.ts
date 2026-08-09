/** Mirrors control-plane manager/commons/dto/ManagerOrgDto (record).
 *  NOTE: the Java DTO has no `owner` or `subscriptionPlan` field — those
 *  were on the previous version of this interface but don't exist on the
 *  wire; dropped rather than invented (see update-org-details-form.service.ts
 *  for the one call site that read `org.subscriptionPlan`, always undefined). */
export interface Org {
  id: Id,
  email: Email,
  createdDate: string,
}

export interface Id {
  id: string
}

export interface Email {
  email: string
}
