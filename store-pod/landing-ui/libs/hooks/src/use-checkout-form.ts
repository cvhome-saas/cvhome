import {useCallback, useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {yupResolver} from "@hookform/resolvers/yup";
import * as Yup from "yup";
import {useTranslations} from "next-intl";
import {Box, defaultCheckoutValue, Order, PaymentType, ReadableCountryList, StoreContext} from "@store-front/types";
import {CartService} from "@store-front/services/cart-service";
import {ContentService} from "@store-front/services/content-service";
import {getCartManager} from "@store-front/services/cart-manager";
import {showToast} from "nextjs-toast-notify";
import {toastDirection} from "@store-front/services/direction-utils";
import {useUser} from "./use-user";

export const useCheckoutForm = (storeContext: StoreContext, requireLoginForOrderPlacement: boolean) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const [successDialogOpen, setSuccessDialogOpen] = useState(false);
    const [agreeDialogOpen, setAgreeDialogOpen] = useState(false);
    const [loginRequiredDialogOpen, setLoginRequiredDialogOpen] = useState(false);
    const [agreement, setAgreement] = useState<Box | undefined>();
    const [order, setOrder] = useState<Order | undefined>();
    const [isAgree, setIsAgree] = useState(false);
    const [readableCountryList, setReadableCountryList] = useState<ReadableCountryList | undefined>();
    const [supportedPaymentTypes, setSupportedPaymentTypes] = useState<PaymentType[] | undefined>();
    const cartManager = getCartManager(storeContext);
    const {user, loading, login} = useUser(storeContext);

    const getSchema = useCallback(() => {
        return Yup.object().shape({
            paymentType: Yup.string().required(t('PAYMENT_TYPE_REQUIRED')),
            customer: Yup.object().shape({
                emailAddress: Yup.string()
                    .required(t('EMAIL_REQUIRED'))
                    .email(t('EMAIL_INVALID')),
                billing: Yup.object().shape({
                    firstName: Yup.string().required(t('FIRST_NAME_REQUIRED')),
                    lastName: Yup.string().required(t('LAST_NAME_REQUIRED')),
                    company: Yup.string().optional().default(''),
                    phone: Yup.string()
                        .required(t('PHONE_REQUIRED'))
                        .matches(/^\d{10,15}$/, t('PHONE_INVALID')),
                    country: Yup.string().required(t('COUNTRY_REQUIRED')),
                    city: Yup.string().required(t('CITY_REQUIRED')),
                    postalCode: Yup.string()
                        .required(t('POSTAL_CODE_REQUIRED'))
                        .matches(/^\d{4,10}$/, t('POSTAL_CODE_INVALID')),
                    address: Yup.string().required(t('ADDRESS_REQUIRED')),
                    isAgree: Yup.boolean().oneOf([true], t('AGREEMENT_AGREE_REQUIRED')),
                }),
            }),
        });
    }, [t]);

    const {
        register,
        setValue,
        control,
        handleSubmit,
        reset,
        formState: {errors},
    } = useForm({
        resolver: yupResolver(getSchema()),
        defaultValues: {
            ...defaultCheckoutValue,
        },
    });

    useEffect(() => {
        setValue("customer.billing.isAgree", isAgree);
    }, [isAgree, setValue]);

    useEffect(() => {
        const fetchCountries = async () => {
            const countries = await CartService.getCountries(storeContext);
            setReadableCountryList(countries);
        };

        const fetchPaymentTypes = async () => {
            const types = await CartService.getSupportedPaymentTypes(storeContext);
            setSupportedPaymentTypes(types);
            if (types && types.length > 0) {
                setValue("paymentType", types[0]);
            }
        };

        fetchCountries().then();
        fetchPaymentTypes().then();
        ContentService.getBox(storeContext, "agreement").then(it => {
            if (it == undefined) {
                setIsAgree(true);
            }
            setAgreement(it);
        });
    }, [storeContext]);

    // Handle initial login required dialog
    useEffect(() => {
        if (!loading && requireLoginForOrderPlacement && !user) {
            setLoginRequiredDialogOpen(true);
        } else {
            setLoginRequiredDialogOpen(false);
        }
    }, [loading, requireLoginForOrderPlacement, user]);

    // Pre-populate form when user data is available
    useEffect(() => {
        if (user) {
            setValue("customer.emailAddress", user.email || "");
            setValue("customer.billing.firstName", user.firstName || "");
            setValue("customer.billing.lastName", user.lastName || "");
        }
    }, [user, setValue]);

    const handleClickOnAgreement = useCallback((event: React.UIEvent) => {
        event.preventDefault();
        setAgreeDialogOpen(true);
    }, []);

    const onSubmit = useCallback(async (checkoutCart: any) => {
        if (loading) return; // Prevent submission while loading user status

        if (requireLoginForOrderPlacement && !user) {
            setLoginRequiredDialogOpen(true);
            return;
        }
        cartManager.checkout(checkoutCart, (o) => {
                setOrder(o);
                if (o) {
                    setSuccessDialogOpen(true);
                    reset();
                }
            }, () => showToast.error(t('FAILED_TO_PLACE_ORDER'), {
                duration: 3000,
                progress: false,
                position: toastDirection(storeContext.locale),
                transition: "bounceIn",
                sound: false,
            })
        );
    }, [cartManager, loading, requireLoginForOrderPlacement, reset, storeContext.locale, t, user]);

    return {
        register,
        setValue,
        control,
        handleSubmit,
        reset,
        errors,
        successDialogOpen,
        setSuccessDialogOpen,
        agreeDialogOpen,
        setAgreeDialogOpen,
        loginRequiredDialogOpen,
        setLoginRequiredDialogOpen,
        agreement,
        order,
        setOrder,
        isAgree,
        setIsAgree,
        readableCountryList,
        supportedPaymentTypes,
        handleClickOnAgreement,
        onSubmit,
        login,
    };
};