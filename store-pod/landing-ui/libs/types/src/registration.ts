/** The body of cua's `POST /api/v1/public/registration`. */
export interface RegistrationRequest {
    username: string;
    email: string;
    password: string;
    firstName?: string;
    lastName?: string;
}

export const emptyRegistration: RegistrationRequest = {username: '', email: '', password: '', firstName: '', lastName: ''};
