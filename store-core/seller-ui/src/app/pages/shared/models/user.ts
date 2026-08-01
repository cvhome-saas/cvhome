/** Mirrors uaa-client domain/user/UserEntity */
export interface UserEntityBase {
  id?: string;
  firstName?: string;
  lastName?: string;
  emailAddress?: string;
  defaultLanguage?: string;
  userName?: string;
  active?: boolean;
}

/** Mirrors uaa-client domain/user/ReadableUser -> UserEntity */
export interface User extends UserEntityBase {
  lastAccess?: string;
  loginTime?: string;
  org?: string;
  store?: string;
  roles?: string[];
}

/** Mirrors uaa-client domain/user/PersistableUser -> UserEntity */
export interface PersistableUser extends UserEntityBase {
  password?: string;
  repeatPassword?: string;
  org?: string;
  store?: string;
  roles?: string[];
}
