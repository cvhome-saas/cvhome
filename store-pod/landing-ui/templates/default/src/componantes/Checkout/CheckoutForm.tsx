'use client'
import {useEffect, useState} from "react";
import {useForm} from "react-hook-form";
import {yupResolver} from "@hookform/resolvers/yup";
import {StoreContext} from "@/types/store-context";
import {CartService} from "@/services/cart-service";
import {getCartCode, setCartData} from "@/services/cart-utils";
import {emitter} from "next/client";
import {ReadableCountryList} from "@/types/country";
import {showToast} from "nextjs-toast-notify";
import {OrderPlacedSuccessfullyDialog} from "@/componantes/Checkout/OrderPlacedSuccessfullyDialog";
import {Order} from "@/types/order";
import {ContentService} from "@/services/content-service";
import {Box} from "@/types/content";
import {CheckoutAgreementDialog} from "@/componantes/Checkout/CheckoutAgreementDialog";
import {useTranslations} from "next-intl";
import * as Yup from "yup";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Textarea} from "@/components/ui/textarea";
import {Checkbox} from "@/components/ui/checkbox";
import {Button} from "@/components/ui/button";
import {Separator} from "@/components/ui/separator";
import {defaultCheckoutValue} from "@/types/checkout-constants";

export const CheckoutForm = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const [successDialogOpen, setSuccessDialogOpen] = useState(false);
    const [agreeDialogOpen, setAgreeDialogOpen] = useState(false);
    const [agreement, setAgreement] = useState<Box | undefined>();
    const [order, setOrder] = useState<Order | undefined>();
    const [isAgree, setIsAgree] = useState(false);
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
    }

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

    const handleClickOnAgreement = (event: React.UIEvent) => {
        event.preventDefault();
        setAgreeDialogOpen(true);
    };

    const onSubmit = async (checkoutCart: any) => {
        const cartCode: string|undefined = getCartCode();
        if (cartCode) {
            const o = await CartService.checkout(storeContext, cartCode, checkoutCart);
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
                onSubmit={handleSubmit(onSubmit)} noValidate
                className="p-6 rounded-lg shadow-md col-span-8 border border-border"
            >
                <h2 className="text-2xl font-bold mb-6 text-foreground">
                    {t('FORM_TITLE')}
                </h2>
                <div className="space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <Label htmlFor="firstName">
                                {t('FIRST_NAME')}
                            </Label>
                            <Input
                                id="firstName"
                                {...register("customer.billing.firstName")}
                                type="text"
                                placeholder={t('FIRST_NAME')}
                                aria-invalid={!!errors.customer?.billing?.firstName}
                            />
                            {errors?.customer?.billing?.firstName && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.firstName.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="lastName">
                                {t('LAST_NAME')}
                            </Label>
                            <Input
                                id="lastName"
                                {...register("customer.billing.lastName")}
                                type="text"
                                placeholder={t('LAST_NAME')}
                                aria-invalid={!!errors.customer?.billing?.lastName}
                            />
                            {errors?.customer?.billing?.lastName && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.lastName.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div className="space-y-2">
                            <Label htmlFor="emailAddress">
                                {t('EMAIL')}
                            </Label>
                            <Input
                                id="emailAddress"
                                {...register("customer.emailAddress")}
                                type="email"
                                placeholder={t('EMAIL')}
                                aria-invalid={!!errors.customer?.emailAddress}
                            />
                            {errors?.customer?.emailAddress && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.emailAddress.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="phone">
                                {t('PHONE')}
                            </Label>
                            <Input
                                id="phone"
                                {...register("customer.billing.phone")}
                                type="tel"
                                placeholder={t('PHONE')}
                                aria-invalid={!!errors.customer?.billing?.phone}
                            />
                            {errors?.customer?.billing?.phone && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.phone.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                        <div className="space-y-2">
                            <Label htmlFor="country">
                                {t('COUNTRY')}
                            </Label>
                            <Select onValueChange={(value) => setValue("customer.billing.country", value)}
                                    defaultValue={defaultCheckoutValue.customer.billing.country}>
                                <SelectTrigger id="country" aria-invalid={!!errors.customer?.billing?.country}>
                                    <SelectValue placeholder={t('SELECT_COUNTRY')}/>
                                </SelectTrigger>
                                <SelectContent>
                                    {readableCountryList?.map((country) => (
                                        <SelectItem key={country.code} value={country.code}>
                                            {country.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {errors?.customer?.billing?.country && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.country.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="city">
                                {t('CITY')}
                            </Label>
                            <Input
                                id="city"
                                {...register("customer.billing.city")}
                                type="text"
                                placeholder={t('CITY')}
                                aria-invalid={!!errors.customer?.billing?.city}
                            />
                            {errors?.customer?.billing?.city && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.city.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="postalCode">
                                {t('POSTAL_CODE')}
                            </Label>
                            <Input
                                id="postalCode"
                                {...register("customer.billing.postalCode")}
                                type="text"
                                placeholder={t('POSTAL_CODE')}
                                aria-invalid={!!errors.customer?.billing?.postalCode}
                            />
                            {errors?.customer?.billing?.postalCode && (
                                <p className="text-sm font-medium text-destructive">{errors.customer.billing.postalCode.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="address">
                            {t('ADDRESS')}
                        </Label>
                        <Textarea
                            id="address"
                            {...register("customer.billing.address")}
                            placeholder={t('ADDRESS')}
                            aria-invalid={!!errors.customer?.billing?.address}
                        />
                        {errors?.customer?.billing?.address && (
                            <p className="text-sm font-medium text-destructive">{errors.customer.billing.address.message}</p>
                        )}
                    </div>
                </div>
                {
                    agreement && (
                        <div className="mt-4 flex items-center">
                            <Checkbox
                                onClick={handleClickOnAgreement}
                                id="isAgree"
                                {...register("customer.billing.isAgree")}
                                checked={isAgree}
                                onCheckedChange={(checked) => setIsAgree(!!checked)}
                                aria-invalid={!!errors.customer?.billing?.isAgree}
                            />
                            <Label htmlFor="isAgree" className="ms-2 text-sm font-medium text-foreground cursor-pointer">
                                {t('AGREEMENT')}
                            </Label>
                            {errors?.customer?.billing?.isAgree && !isAgree && (
                                <p className="text-sm font-medium text-destructive ms-2">{errors.customer.billing.isAgree.message}</p>
                            )}
                        </div>
                    )
                }

                <div className="mt-6">
                    <Separator className="my-4"/>
                    <div className="flex justify-center">
                        <Button
                            type="submit"
                            size="lg"
                        >
                            {t('PLACE_ORDER')}
                        </Button>
                    </div>
                </div>
            </form>
        </>
    );
};