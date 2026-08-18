export interface User {
  readonly id?: string;
  readonly firstName?: string;
  readonly lastName?: string;
  readonly emailAddress: string;
  readonly active?: boolean;
  readonly groups?: readonly string[];
}

export interface PersistableUser extends User {
  readonly password?: string;
}
