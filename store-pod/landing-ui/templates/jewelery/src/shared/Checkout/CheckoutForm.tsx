'use client'
import {useTranslations} from "next-intl";
import {StoreContext} from "@/types/store-context";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Textarea} from "@/components/ui/textarea";
import {Checkbox} from "@/components/ui/checkbox";
import {Separator} from "@/components/ui/separator";
import {defaultCheckoutValue} from "@/types/checkout-constants";
import {CheckCircle, HelpCircle} from "lucide-react";
import {parseDescription} from "@/services/description-view-util";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogCancel,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle
} from "@/components/ui/alert-dialog";
import {useRouter} from "@/i18n/navigation";
import {Box} from "@/types/content";
import {Order} from "@/types/order";
import {useCheckoutForm} from "@store-front/hooks/use-checkout-form";
import {CartProductList} from "@/shared/Cart/CartProductList";
import {ScrollArea} from "@/components/ui/scroll-area";
import {useCart} from "@store-front/hooks/use-cart";

export const CheckoutForm = ({storeContext, requireLoginForOrderPlacement}: {
    storeContext: StoreContext,
    requireLoginForOrderPlacement: boolean
}) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const {
        register,
        setValue,
        handleSubmit,
        errors,
        successDialogOpen,
        setSuccessDialogOpen,
        agreeDialogOpen,
        setAgreeDialogOpen,
        loginRequiredDialogOpen,
        setLoginRequiredDialogOpen,
        agreement,
        order,
        isAgree,
        setIsAgree,
        readableCountryList,
        supportedPaymentTypes,
        handleClickOnAgreement,
        onSubmit,
        login,
    } = useCheckoutForm(storeContext, requireLoginForOrderPlacement);

    return (
        <>
            <OrderPlacedSuccessfullyDialog order={order} isOpen={successDialogOpen} setIsOpen={setSuccessDialogOpen}/>
            <CheckoutAgreementDialog box={agreement} isOpen={agreeDialogOpen} setIsOpen={setAgreeDialogOpen}
                                     setIsAgree={setIsAgree}/>
            <LoginRequiredDialog isOpen={loginRequiredDialogOpen} setIsOpen={setLoginRequiredDialogOpen} login={login}/>

            <form
                onSubmit={handleSubmit(onSubmit)}
                noValidate
                className="col-span-8 border border-border p-8"
            >
                <h2 className="font-['Cormorant_Garamond',serif] text-2xl font-light tracking-wide text-foreground mb-8">
                    {t('FORM_TITLE')}
                </h2>

                <div className="space-y-6">
                    {/* Name row */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <div className="space-y-2">
                            <Label htmlFor="firstName"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('FIRST_NAME')}
                            </Label>
                            <Input
                                id="firstName"
                                {...register("customer.billing.firstName")}
                                type="text"
                                placeholder={t('FIRST_NAME')}
                                aria-invalid={!!errors.customer?.billing?.firstName}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.billing?.firstName && (
                                <p className="text-xs text-destructive">{errors.customer.billing.firstName.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="lastName"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('LAST_NAME')}
                            </Label>
                            <Input
                                id="lastName"
                                {...register("customer.billing.lastName")}
                                type="text"
                                placeholder={t('LAST_NAME')}
                                aria-invalid={!!errors.customer?.billing?.lastName}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.billing?.lastName && (
                                <p className="text-xs text-destructive">{errors.customer.billing.lastName.message}</p>
                            )}
                        </div>
                    </div>

                    {/* Contact row */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <div className="space-y-2">
                            <Label htmlFor="emailAddress"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('EMAIL')}
                            </Label>
                            <Input
                                id="emailAddress"
                                {...register("customer.emailAddress")}
                                type="email"
                                placeholder={t('EMAIL')}
                                aria-invalid={!!errors.customer?.emailAddress}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.emailAddress && (
                                <p className="text-xs text-destructive">{errors.customer.emailAddress.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="phone"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('PHONE')}
                            </Label>
                            <Input
                                id="phone"
                                {...register("customer.billing.phone")}
                                type="tel"
                                placeholder={t('PHONE')}
                                aria-invalid={!!errors.customer?.billing?.phone}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.billing?.phone && (
                                <p className="text-xs text-destructive">{errors.customer.billing.phone.message}</p>
                            )}
                        </div>
                    </div>

                    {/* Location row */}
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                        <div className="space-y-2">
                            <Label htmlFor="country"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('COUNTRY')}
                            </Label>
                            <Select
                                onValueChange={(value) => setValue("customer.billing.country", value)}
                                defaultValue={defaultCheckoutValue.customer.billing.country}
                            >
                                <SelectTrigger id="country" aria-invalid={!!errors.customer?.billing?.country}
                                               className="rounded-none border-border text-sm">
                                    <SelectValue placeholder={t('SELECT_COUNTRY')}/>
                                </SelectTrigger>
                                <SelectContent className="rounded-none">
                                    {readableCountryList?.map((country) => (
                                        <SelectItem key={country.code} value={country.code} className="text-sm">
                                            {country.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {errors?.customer?.billing?.country && (
                                <p className="text-xs text-destructive">{errors.customer.billing.country.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="city"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('CITY')}
                            </Label>
                            <Input
                                id="city"
                                {...register("customer.billing.city")}
                                type="text"
                                placeholder={t('CITY')}
                                aria-invalid={!!errors.customer?.billing?.city}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.billing?.city && (
                                <p className="text-xs text-destructive">{errors.customer.billing.city.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="postalCode"
                                   className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                                {t('POSTAL_CODE')}
                            </Label>
                            <Input
                                id="postalCode"
                                {...register("customer.billing.postalCode")}
                                type="text"
                                placeholder={t('POSTAL_CODE')}
                                aria-invalid={!!errors.customer?.billing?.postalCode}
                                className="rounded-none border-border focus:border-primary focus:ring-primary text-sm"
                            />
                            {errors?.customer?.billing?.postalCode && (
                                <p className="text-xs text-destructive">{errors.customer.billing.postalCode.message}</p>
                            )}
                        </div>
                    </div>

                    {/* Address */}
                    <div className="space-y-2">
                        <Label htmlFor="address"
                               className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                            {t('ADDRESS')}
                        </Label>
                        <Textarea
                            id="address"
                            {...register("customer.billing.address")}
                            placeholder={t('ADDRESS')}
                            aria-invalid={!!errors.customer?.billing?.address}
                            className="rounded-none border-border focus:border-primary focus:ring-primary text-sm resize-none"
                            rows={3}
                        />
                        {errors?.customer?.billing?.address && (
                            <p className="text-xs text-destructive">{errors.customer.billing.address.message}</p>
                        )}
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="paymentType"
                               className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">
                            {t('PAYMENT_TYPE')}
                        </Label>
                        <Select onValueChange={(value) => setValue("paymentType", value)}
                                value={supportedPaymentTypes?.length ? undefined : defaultCheckoutValue.paymentType}>
                            <SelectTrigger id="paymentType" aria-invalid={!!errors.paymentType}
                                           className="rounded-none border-border text-sm">
                                <SelectValue placeholder={t('SELECT_PAYMENT_TYPE')}/>
                            </SelectTrigger>
                            <SelectContent className="rounded-none">
                                {supportedPaymentTypes?.map((type) => (
                                    <SelectItem key={type} value={type} className="text-sm">
                                        {t(`PAYMENT_TYPES.${type}`)}
                                    </SelectItem>
                                ))}
                            </SelectContent>
                        </Select>
                        {errors?.paymentType && (
                            <p className="text-xs text-destructive">{errors.paymentType.message}</p>
                        )}
                    </div>
                </div>

                {/* Agreement checkbox */}
                {agreement && (
                    <div className="mt-6 flex items-center gap-3">
                        <Checkbox
                            onClick={handleClickOnAgreement}
                            id="isAgree"
                            {...register("customer.billing.isAgree")}
                            checked={isAgree}
                            onCheckedChange={(checked) => setIsAgree(!!checked)}
                            aria-invalid={!!errors.customer?.billing?.isAgree}
                            className="rounded-none"
                        />
                        <Label htmlFor="isAgree" className="text-xs tracking-wider cursor-pointer text-foreground">
                            {t('AGREEMENT')}
                        </Label>
                        {errors?.customer?.billing?.isAgree && !isAgree && (
                            <p className="text-xs text-destructive">{errors.customer.billing.isAgree.message}</p>
                        )}
                    </div>
                )}

                <div className="mt-8">
                    <Separator className="mb-8"/>
                    <div className="flex justify-center">
                        <button
                            type="submit"
                            className="px-12 py-3.5 text-xs tracking-[0.25em] uppercase font-medium border border-foreground bg-foreground text-background hover:bg-primary hover:border-primary transition-all duration-300"
                        >
                            {t('PLACE_ORDER')}
                        </button>
                    </div>
                </div>
            </form>
        </>
    );
};

const CheckoutAgreementDialog = ({box, isOpen, setIsOpen, setIsAgree}: {
    box: Box | undefined
    isOpen: boolean,
    setIsOpen: (x: boolean) => void,
    setIsAgree: (x: boolean) => void,
}) => {
    const t = useTranslations('PAGE.CHECKOUT');

    const handleAgree = () => {
        setIsAgree(true);
        setIsOpen(false);
    };

    const handleReject = () => {
        setIsAgree(false);
        setIsOpen(false);
    };

    return (
        <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
            <AlertDialogContent className="rounded-none">
                <AlertDialogHeader>
                    <div className="flex justify-center mb-2">
                        <HelpCircle className="h-12 w-12 text-primary"/>
                    </div>
                    <AlertDialogTitle
                        className="text-center font-['Cormorant_Garamond',serif] text-xl font-light tracking-wide">
                        {t('TERMS_AND_CONDITIONS')}
                    </AlertDialogTitle>
                    {box && (
                        <AlertDialogDescription
                            className="max-h-60 overflow-y-auto text-foreground text-sm leading-relaxed"
                            dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}
                        />
                    )}
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel asChild>
                        <button
                            onClick={handleReject}
                            className="px-6 py-2 text-xs tracking-widest uppercase border border-border text-foreground hover:bg-accent transition-colors"
                        >
                            {t('REJECT')}
                        </button>
                    </AlertDialogCancel>
                    <AlertDialogAction asChild>
                        <button
                            onClick={handleAgree}
                            className="px-6 py-2 text-xs tracking-widest uppercase bg-foreground text-background hover:bg-primary transition-colors"
                        >
                            {t('AGREE')}
                        </button>
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};

export const OrderPlacedSuccessfullyDialog = ({order, isOpen, setIsOpen}: {
    order: Order | undefined
    isOpen: boolean,
    setIsOpen: (x: boolean) => void
}) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const router = useRouter();

    const handleContinueShopping = () => {
        setIsOpen(false);
        router.push("/");
    };

    return (
        <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
            <AlertDialogContent className="rounded-none">
                <AlertDialogHeader className="items-center text-center">
                    <CheckCircle className="h-12 w-12 text-primary mb-2"/>
                    <AlertDialogTitle className="font-['Cormorant_Garamond',serif] text-xl font-light tracking-wide">
                        {t('ORDER_PLACED_SUCCESSFULLY')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="space-y-2 text-sm">
                        {order ? (
                            <>
                                <p>{t('ORDER_PLACED_SUCCESSFULLY')}</p>
                                <p><strong className="font-medium">{t('ORDER_ID')}:</strong> {order.id}</p>
                            </>
                        ) : (
                            <p>{t('NO_ORDER_DETAILED')}</p>
                        )}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogAction asChild>
                        <button
                            onClick={handleContinueShopping}
                            className="w-full px-6 py-3 text-xs tracking-widest uppercase bg-foreground text-background hover:bg-primary transition-colors"
                        >
                            {t('CONTINUE_SHOPPING')}
                        </button>
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};

export const LoginRequiredDialog = ({isOpen, setIsOpen, login}: {
    isOpen: boolean,
    setIsOpen: (x: boolean) => void,
    login: () => void,
}) => {
    const t = useTranslations('PAGE.CHECKOUT');

    return (
        <AlertDialog open={isOpen} onOpenChange={setIsOpen}>
            <AlertDialogContent className="rounded-none">
                <AlertDialogHeader className="items-center text-center">
                    <HelpCircle className="h-12 w-12 text-primary mb-2"/>
                    <AlertDialogTitle className="font-['Cormorant_Garamond',serif] text-xl font-light tracking-wide">
                        {t('LOGIN_REQUIRED_TITLE')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="text-sm">
                        {t('LOGIN_REQUIRED_DESCRIPTION')}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center">
                    <AlertDialogCancel
                        className="rounded-none text-xs tracking-widest uppercase">{t('CANCEL')}</AlertDialogCancel>
                    <AlertDialogAction
                        onClick={login}
                        className="rounded-none text-xs tracking-widest uppercase bg-foreground text-background hover:bg-primary"
                    >
                        {t('LOGIN')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};

export const CheckoutCartBox = ({storeContext}: { storeContext: StoreContext }) => {
    const t = useTranslations('PAGE.CHECKOUT');
    const {cart} = useCart(storeContext);

    return (
        <div className="border border-border col-span-4">
            <div className="px-6 py-5 border-b border-border">
                <h3 className="font-['Cormorant_Garamond',serif] text-lg font-light tracking-wide text-foreground">
                    {t('CART_DETAILS')}
                </h3>
            </div>
            <div className="px-6 py-4">
                <ScrollArea className="h-80">
                    <CartProductList storeContext={storeContext} cart={cart}/>
                </ScrollArea>
            </div>
            {cart && (
                <div className="px-6 py-5 border-t border-border">
                    <div className="flex justify-between items-center">
                        <span
                            className="text-[10px] tracking-[0.2em] uppercase font-medium text-foreground">{t('SUB_TOTAL')}</span>
                        <span
                            className="font-['Cormorant_Garamond',serif] text-lg font-light text-foreground">{cart.displaySubTotal}</span>
                    </div>
                </div>
            )}
        </div>
    );
};
