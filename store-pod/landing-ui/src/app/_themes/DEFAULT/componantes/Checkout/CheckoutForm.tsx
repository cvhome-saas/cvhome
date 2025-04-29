'use client'
import {useEffect, useState} from "react";
import {FieldValues, useForm} from "react-hook-form";
import {yupResolver} from "@hookform/resolvers/yup";
import {CheckoutCart} from "@/types/checkout-cart";
import {StoreContext} from "@/types/store-context";
import {CartService} from "@/services/cart-service";
import {getCartCode, setCartData} from "@/utils/cart-utils";
import {emitter} from "next/client";
import {ReadableCountryList} from "@/types/country";
import {showToast} from "nextjs-toast-notify";
import {OrderPlacedSuccessfullyDialog} from "@/app/_themes/DEFAULT/componantes/Checkout/OrderPlacedSuccessfullyDialog";
import {Order} from "@/types/order";
import {defaultCheckoutValue} from "@/types/checkout-constants";
import {defaultClass, errorClass, focusDefaultClass, focusErrorClass} from "@/types/constant";
import {ContentService} from "@/services/content-service";
import {Box} from "@/types/content";
import {CheckoutAgreementDialog} from "@/app/_themes/DEFAULT/componantes/Checkout/CheckoutAgreementDialog";
import {useTranslations} from "next-intl";
import * as Yup from "yup";

export const CheckoutForm = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    let [successDialogOpen, setSuccessDialogOpen] = useState(false);
    let [agreeDialogOpen, setAgreeDialogOpen] = useState(false);
    let [agreement, setAgreement] = useState<Box | undefined>();
    let [order, setOrder] = useState<Order | undefined>();
    let [isAgree, setIsAgree] = useState(false);
    const [readableCountryList, setReadableCountryList] = useState<ReadableCountryList | undefined>();

    function getSchema() {
        return Yup.object().shape({
            payment: Yup.object().shape({
                paymentType: Yup.string().required(t('PAYMENT_TYPE_REQUIRED')),
                transactionType: Yup.string().required(t('TRANSACTION_TYPE_REQUIRED')),
            }),
            customer: Yup.object().shape({
                emailAddress: Yup.string()
                    .required(t('EMAIL_REQUIRED'))
                    .email(t('EMAIL_INVALID')),
                billing: Yup.object().shape({
                    firstName: Yup.string().required(t('FIRST_NAME_REQUIRED')),
                    lastName: Yup.string().required(t('LAST_NAME_REQUIRED')),
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
    }

    const {
        register,
        setValue,
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
    }, [isAgree]);

    useEffect(() => {
        const fetchCountries = async () => {
            const countries = await CartService.getCountries(storeContext);
            setReadableCountryList(countries);
        };

        fetchCountries().then();
        ContentService.getBox(storeContext, "agreement").then(it => {
            if (it == undefined) {
                setIsAgree(true);
            }
            setAgreement(it);
        });
    }, []);

    const handleClickOnAgreement = (event: React.MouseEvent<HTMLInputElement>) => {
        event.preventDefault();
        setAgreeDialogOpen(true);
    };

    const onSubmit = async (data: FieldValues) => {
        const checkoutCart = data as unknown as CheckoutCart;
        let cartCode = getCartCode();
        if (cartCode) {
            let o = await CartService.checkout(storeContext, cartCode, checkoutCart);
            setOrder(o);
            if (o) {
                setCartData(undefined);
                setSuccessDialogOpen(true);
                reset();
                emitter.emit("CART_STORAGE_CHANGED", "");
            } else {
                showToast.error(t('FAILED_TO_PLACE_ORDER'), {
                    duration: 3000,
                    progress: false,
                    position: "bottom-right",
                    transition: "bounceIn",
                    sound: false,
                });
            }
        }
    };

    return (
        <>
            <OrderPlacedSuccessfullyDialog order={order} isOpen={successDialogOpen} setIsOpen={setSuccessDialogOpen}/>
            <CheckoutAgreementDialog box={agreement} isOpen={agreeDialogOpen} setIsOpen={setAgreeDialogOpen}
                                     isAgree={isAgree} setIsAgree={setIsAgree}/>
            <form
                onSubmit={handleSubmit(onSubmit)}
                className="p-6 rounded-lg shadow-md col-span-8 border-2 border-border"
            >
                <h2 className="text-2xl font-bold mb-6 text-foreground">
                    {t('FORM_TITLE')}
                </h2>
                <div className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label htmlFor="firstName" className="block text-sm font-medium text-foreground">
                                {t('FIRST_NAME')}
                            </label>
                            <input
                                id="firstName"
                                {...register("customer.billing.firstName")}
                                type="text"
                                placeholder={t('FIRST_NAME')}
                                className={`border ${
                                    errors?.customer?.billing?.firstName ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.firstName ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.billing?.firstName && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.firstName.message}</p>
                            )}
                        </div>
                        <div>
                            <label htmlFor="lastName" className="block text-sm font-medium text-foreground">
                                {t('LAST_NAME')}
                            </label>
                            <input
                                id="lastName"
                                {...register("customer.billing.lastName")}
                                type="text"
                                placeholder={t('LAST_NAME')}
                                className={`border ${
                                    errors?.customer?.billing?.lastName ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.lastName ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.billing?.lastName && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.lastName.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label htmlFor="email" className="block text-sm font-medium text-foreground">
                                {t('EMAIL')}
                            </label>
                            <input
                                id="emailAddress"
                                {...register("customer.emailAddress")}
                                type="email"
                                placeholder={t('EMAIL')}
                                className={`border ${
                                    errors?.customer?.emailAddress ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.emailAddress ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.emailAddress && (
                                <p className="text-error text-sm">{errors?.customer?.emailAddress.message}</p>
                            )}
                        </div>
                        <div>
                            <label htmlFor="phone" className="block text-sm font-medium text-foreground">
                                {t('PHONE')}
                            </label>
                            <input
                                id="phone"
                                {...register("customer.billing.phone")}
                                type="tel"
                                placeholder={t('PHONE')}
                                className={`border ${
                                    errors?.customer?.billing?.phone ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.phone ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.billing?.phone && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.phone.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div>
                            <label htmlFor="country" className="block text-sm font-medium text-foreground">
                                {t('COUNTRY')}
                            </label>
                            <select
                                id="country"
                                {...register("customer.billing.country")}
                                className={`border ${
                                    errors?.customer?.billing?.country ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.country ? focusErrorClass : focusDefaultClass
                                }`}
                            >
                                <option value="">{t('SELECT_COUNTRY')}</option>
                                {readableCountryList &&
                                    readableCountryList.map((country) => (
                                        <option key={country.code} value={country.code}>
                                            {country.name}
                                        </option>
                                    ))}
                            </select>
                            {errors?.customer?.billing?.country && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.country.message}</p>
                            )}
                        </div>
                        <div>
                            <label htmlFor="city" className="block text-sm font-medium text-foreground">
                                {t('CITY')}
                            </label>
                            <input
                                id="city"
                                {...register("customer.billing.city")}
                                type="text"
                                placeholder={t('CITY')}
                                className={`border ${
                                    errors?.customer?.billing?.city ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.city ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.billing?.city && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.city.message}</p>
                            )}
                        </div>
                        <div>
                            <label htmlFor="postalCode" className="block text-sm font-medium text-foreground">
                                {t('POSTAL_CODE')}
                            </label>
                            <input
                                id="postalCode"
                                {...register("customer.billing.postalCode")}
                                type="text"
                                placeholder={t('POSTAL_CODE')}
                                className={`border ${
                                    errors?.customer?.billing?.postalCode ? errorClass : defaultClass
                                } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.postalCode ? focusErrorClass : focusDefaultClass
                                }`}
                            />
                            {errors?.customer?.billing?.postalCode && (
                                <p className="text-error text-sm">{errors?.customer?.billing?.postalCode.message}</p>
                            )}
                        </div>
                    </div>

                    <div>
                        <label htmlFor="address" className="block text-sm font-medium text-foreground">
                            {t('ADDRESS')}
                        </label>
                        <textarea
                            id="address"
                            {...register("customer.billing.address")}
                            placeholder={t('ADDRESS')}
                            className={`border ${
                                errors?.customer?.billing?.address ? errorClass : defaultClass
                            } p-2 rounded-md w-full focus:outline-none focus:ring-2 ${
                                errors?.customer?.billing?.address ? focusErrorClass : focusDefaultClass
                            }`}
                        />
                        {errors?.customer?.billing?.address && (
                            <p className="text-error text-sm">{errors?.customer?.billing?.address.message}</p>
                        )}
                    </div>
                </div>
                {
                    agreement && (
                        <div className="mt-4 flex items-center">
                            <input
                                onClick={handleClickOnAgreement}
                                id="isAgree"
                                {...register("customer.billing.isAgree")}
                                checked={isAgree}
                                onChange={(e) => setIsAgree(e.target.checked)}
                                type="checkbox"
                                className={`border ${
                                    errors?.customer?.billing?.isAgree ? errorClass : defaultClass
                                } p-2 rounded focus:outline-none focus:ring-2 ${
                                    errors?.customer?.billing?.isAgree ? focusErrorClass : focusDefaultClass
                                } accent-primary`}
                            />
                            <label htmlFor="isAgree" className="ms-2 text-sm font-medium text-foreground cursor-pointer">
                                {t('AGREEMENT')}
                            </label>
                            {errors?.customer?.billing?.isAgree && !isAgree && (
                                <p className="text-error text-sm ms-2">{errors?.customer?.billing?.isAgree.message}</p>
                            )}
                        </div>
                    )
                }

                <div className="mt-6">
                    <hr className="my-4 border-border"/>
                    <div className="flex justify-center">
                        <button
                            type="submit"
                            className="bg-primary text-foreground font-bold py-2 px-6 rounded-lg w-auto hover:bg-hover-primary transition duration-200"
                        >
                            {t('PLACE_ORDER')}
                        </button>
                    </div>
                </div>
            </form>
        </>
    );
};