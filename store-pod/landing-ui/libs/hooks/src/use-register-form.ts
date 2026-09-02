import {useCallback, useState} from "react";
import {useTranslations} from "next-intl";
import {emptyRegistration, isApiError, ProblemFieldError, RegistrationRequest, StoreContext} from "@store-front/types";
import {AuthService} from "@store-front/services/auth-service";
import {useErrorMessage} from "./use-error-message";

export type RegistrationField = keyof RegistrationRequest;

export type RegistrationFieldErrors = Partial<Record<RegistrationField, string>>;

/** Which control a typed conflict belongs under. */
const CONFLICT_FIELDS: Readonly<Record<string, RegistrationField>> = {
    'CUA.REGISTRATION.USERNAME_TAKEN': 'username',
    'CUA.REGISTRATION.EMAIL_TAKEN': 'email',
};

const FIELDS: readonly RegistrationField[] = ['username', 'email', 'password', 'firstName', 'lastName'];

function isField(name: string): name is RegistrationField {
    return (FIELDS as readonly string[]).includes(name);
}

/**
 * The storefront's registration form: values, per-field errors, and a submit that hands a new shopper straight
 * into the login flow.
 *
 * Errors come from the server's own validation rather than a duplicated client schema: cua answers a bad body
 * with `fieldErrors` (one per control, coded `VALIDATION.<constraint>`) and a collision with a typed conflict,
 * and both are mapped onto the control they belong to. What cannot be pinned to a control becomes `error`.
 */
export function useRegisterForm(storeContext: StoreContext) {
    const t = useTranslations('PAGE.REGISTER');
    const messageFor = useErrorMessage();
    const [values, setValues] = useState<RegistrationRequest>(emptyRegistration);
    const [submitting, setSubmitting] = useState(false);
    const [fieldErrors, setFieldErrors] = useState<RegistrationFieldErrors>({});
    const [error, setError] = useState<string | undefined>();

    const fieldMessage = useCallback((failure: ProblemFieldError): string => {
        const key = `FIELD_ERRORS.${failure.code.replace(/\./g, '_')}`;
        return t.has(key) ? t(key) : t('FIELD_INVALID');
    }, [t]);

    const set = useCallback((field: RegistrationField, value: string) => {
        setValues(current => ({...current, [field]: value}));
        setFieldErrors(current => (current[field] ? {...current, [field]: undefined} : current));
    }, []);

    const submit = useCallback(async () => {
        setSubmitting(true);
        setError(undefined);
        setFieldErrors({});
        try {
            await AuthService.register(storeContext, values);
            // The account exists; sign in through the normal flow and land on the home page rather than back here.
            await AuthService.login(storeContext, {returnTo: `/${storeContext.locale}`});
        } catch (failure) {
            if (isApiError(failure)) {
                const conflictField = CONFLICT_FIELDS[failure.code];
                if (conflictField) {
                    setFieldErrors({[conflictField]: messageFor(failure)});
                } else if (failure.fieldErrors.length > 0) {
                    const next: RegistrationFieldErrors = {};
                    for (const fieldError of failure.fieldErrors) {
                        if (isField(fieldError.field) && !next[fieldError.field]) {
                            next[fieldError.field] = fieldMessage(fieldError);
                        }
                    }
                    setFieldErrors(next);
                } else {
                    setError(messageFor(failure, t('FAILED')));
                }
            } else {
                setError(messageFor(failure, t('FAILED')));
            }
            setSubmitting(false);
        }
    }, [storeContext, values, messageFor, fieldMessage, t]);

    return {values, set, submit, submitting, fieldErrors, error};
}
