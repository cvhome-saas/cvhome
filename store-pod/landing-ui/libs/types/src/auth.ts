export enum AuthEventType {
    LOGIN = 'LOGIN',
    LOGOUT = 'LOGOUT',
}

export interface AuthEventDetail {
    type: AuthEventType;
}
