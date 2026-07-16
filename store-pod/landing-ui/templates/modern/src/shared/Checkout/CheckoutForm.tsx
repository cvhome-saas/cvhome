'use client'
import {useTranslations} from "next-intl";
import {StoreContext} from "@/types/store-context";
import {Input} from "@/components/ui/input";
import {Label} from "@/components/ui/label";
import {Select, SelectContent, SelectItem, SelectTrigger, SelectValue} from "@/components/ui/select";
import {Textarea} from "@/components/ui/textarea";
import {Checkbox} from "@/components/ui/checkbox";
import {Button} from "@/components/ui/button";
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
import {Card, CardContent, CardFooter, CardHeader, CardTitle} from "@/components/ui/card";
import {ScrollArea} from "@/components/ui/scroll-area";
import {useCart} from "@store-front/hooks/use-cart";
import {cn} from "@/lib/utils";

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
            <CheckoutAgreementDialog
                box={agreement}
                isOpen={agreeDialogOpen}
                setIsOpen={setAgreeDialogOpen}
                setIsAgree={setIsAgree}
            />
            <LoginRequiredDialog isOpen={loginRequiredDialogOpen} setIsOpen={setLoginRequiredDialogOpen} login={login}/>

            <Card className="col-span-8 rounded-2xl border border-border bg-background shadow-none">
                <CardHeader className="pb-4">
                    <CardTitle className="text-2xl font-semibold tracking-tight text-foreground">
                        {t('FORM_TITLE')}
                    </CardTitle>
                </CardHeader>

                <CardContent>
                    <form onSubmit={handleSubmit(onSubmit)} noValidate className="space-y-8">
                        <section className="rounded-2xl p-5">

                            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                                <div className="space-y-2">
                                    <Label htmlFor="firstName">{t('FIRST_NAME')}</Label>
                                    <Input
                                        id="firstName"
                                        {...register("customer.billing.firstName")}
                                        type="text"
                                        placeholder={t('FIRST_NAME')}
                                        aria-invalid={!!errors.customer?.billing?.firstName}
                                    />
                                    {errors?.customer?.billing?.firstName && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.firstName.message}
                                        </p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="lastName">{t('LAST_NAME')}</Label>
                                    <Input
                                        id="lastName"
                                        {...register("customer.billing.lastName")}
                                        type="text"
                                        placeholder={t('LAST_NAME')}
                                        aria-invalid={!!errors.customer?.billing?.lastName}
                                    />
                                    {errors?.customer?.billing?.lastName && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.lastName.message}
                                        </p>
                                    )}
                                </div>
                            </div>

                            <div className="mt-4 grid grid-cols-1 gap-4 md:grid-cols-2">
                                <div className="space-y-2">
                                    <Label htmlFor="emailAddress">{t('EMAIL')}</Label>
                                    <Input
                                        id="emailAddress"
                                        {...register("customer.emailAddress")}
                                        type="email"
                                        placeholder={t('EMAIL')}
                                        aria-invalid={!!errors.customer?.emailAddress}
                                    />
                                    {errors?.customer?.emailAddress && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.emailAddress.message}
                                        </p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="phone">{t('PHONE')}</Label>
                                    <Input
                                        id="phone"
                                        {...register("customer.billing.phone")}
                                        type="tel"
                                        placeholder={t('PHONE')}
                                        aria-invalid={!!errors.customer?.billing?.phone}
                                    />
                                    {errors?.customer?.billing?.phone && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.phone.message}
                                        </p>
                                    )}
                                </div>
                            </div>
                        </section>

                        <section className="rounded-2xl  p-5">

                            <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                                <div className="space-y-2">
                                    <Label htmlFor="country">{t('COUNTRY')}</Label>
                                    <Select
                                        onValueChange={(value) => setValue("customer.billing.country", value)}
                                        defaultValue={defaultCheckoutValue.customer.billing.country}
                                    >
                                        <SelectTrigger id="country" aria-invalid={!!errors.customer?.billing?.country}
                                                       className="w-full">
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
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.country.message}
                                        </p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="city">{t('CITY')}</Label>
                                    <Input
                                        id="city"
                                        {...register("customer.billing.city")}
                                        type="text"
                                        placeholder={t('CITY')}
                                        aria-invalid={!!errors.customer?.billing?.city}
                                    />
                                    {errors?.customer?.billing?.city && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.city.message}
                                        </p>
                                    )}
                                </div>

                                <div className="space-y-2">
                                    <Label htmlFor="postalCode">{t('POSTAL_CODE')}</Label>
                                    <Input
                                        id="postalCode"
                                        {...register("customer.billing.postalCode")}
                                        type="text"
                                        placeholder={t('POSTAL_CODE')}
                                        aria-invalid={!!errors.customer?.billing?.postalCode}
                                    />
                                    {errors?.customer?.billing?.postalCode && (
                                        <p className="text-sm font-medium text-destructive">
                                            {errors.customer.billing.postalCode.message}
                                        </p>
                                    )}
                                </div>
                            </div>

                            <div className="mt-4 space-y-2">
                                <Label htmlFor="address">{t('ADDRESS')}</Label>
                                <Textarea
                                    id="address"
                                    {...register("customer.billing.address")}
                                    placeholder={t('ADDRESS')}
                                    aria-invalid={!!errors.customer?.billing?.address}
                                />
                                {errors?.customer?.billing?.address && (
                                    <p className="text-sm font-medium text-destructive">
                                        {errors.customer.billing.address.message}
                                    </p>
                                )}
                            </div>

                            <div className="mt-4 space-y-2">
                                <Label htmlFor="paymentType">{t('PAYMENT_TYPE')}</Label>
                                <Select
                                    onValueChange={(value) => setValue("paymentType", value)}
                                    value={supportedPaymentTypes?.length ? undefined : defaultCheckoutValue.paymentType}
                                >
                                    <SelectTrigger id="paymentType" aria-invalid={!!errors.paymentType}
                                                   className="w-full">
                                        <SelectValue placeholder={t('SELECT_PAYMENT_TYPE')}/>
                                    </SelectTrigger>
                                    <SelectContent>
                                        {supportedPaymentTypes?.map((type) => (
                                            <SelectItem key={type} value={type}>
                                                {t(`PAYMENT_TYPES.${type}`)}
                                            </SelectItem>
                                        ))}
                                    </SelectContent>
                                </Select>
                                {errors?.paymentType && (
                                    <p className="text-sm font-medium text-destructive">
                                        {errors.paymentType.message}
                                    </p>
                                )}
                            </div>
                        </section>

                        {agreement && (
                            <section className="rounded-2xl  bg-background p-5">
                                <div className="flex items-start gap-3">
                                    <Checkbox
                                        onClick={handleClickOnAgreement}
                                        id="isAgree"
                                        {...register("customer.billing.isAgree")}
                                        checked={isAgree}
                                        onCheckedChange={(checked) => setIsAgree(!!checked)}
                                        aria-invalid={!!errors.customer?.billing?.isAgree}
                                    />
                                    <div className="min-w-0">
                                        <Label
                                            htmlFor="isAgree"
                                            className={cn(
                                                "cursor-pointer text-sm font-medium text-foreground",
                                                "tracking-wide"
                                            )}
                                        >
                                            {t('AGREEMENT')}
                                        </Label>
                                        <p className="mt-1 text-xs text-muted-foreground">
                                            {t('TERMS_AND_CONDITIONS')}
                                        </p>
                                        {errors?.customer?.billing?.isAgree && !isAgree && (
                                            <p className="mt-2 text-sm font-medium text-destructive">
                                                {errors.customer.billing.isAgree.message}
                                            </p>
                                        )}
                                    </div>
                                </div>
                            </section>
                        )}

                        <div className="flex justify-end">
                            <Button type="submit" size="lg" className="h-12 rounded-xl px-8">
                                {t('PLACE_ORDER')}
                            </Button>
                        </div>
                    </form>
                </CardContent>
            </Card>
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
            <AlertDialogContent className="rounded-2xl">
                <AlertDialogHeader>
                    <div className="flex justify-center">
                        <HelpCircle className="h-14 w-14 text-primary"/>
                    </div>
                    <AlertDialogTitle className="text-center text-sm tracking-[0.18em] uppercase">
                        {t('TERMS_AND_CONDITIONS')}
                    </AlertDialogTitle>
                    {box && (
                        <AlertDialogDescription
                            className="max-h-72 overflow-y-auto text-foreground"
                            dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}
                        />
                    )}
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel asChild>
                        <Button variant="outline" className="rounded-xl" onClick={handleReject}>
                            {t('REJECT')}
                        </Button>
                    </AlertDialogCancel>
                    <AlertDialogAction asChild>
                        <Button className="rounded-xl" onClick={handleAgree}>
                            {t('AGREE')}
                        </Button>
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
            <AlertDialogContent className="rounded-2xl">
                <AlertDialogHeader className="items-center text-center">
                    <CheckCircle className="h-14 w-14 text-green-500"/>
                    <AlertDialogTitle className="text-lg">
                        {t('ORDER_PLACED_SUCCESSFULLY')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="space-y-2">
                        {order ? (
                            <>
                                <p>{t('ORDER_PLACED_SUCCESSFULLY')}</p>
                                <p>
                                    <strong>{t('ORDER_ID')}:</strong> {order.id}
                                </p>
                            </>
                        ) : (
                            <p>{t('NO_ORDER_DETAILED')}</p>
                        )}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogAction onClick={handleContinueShopping} className="w-full rounded-xl">
                        {t('CONTINUE_SHOPPING')}
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
            <AlertDialogContent className="rounded-2xl">
                <AlertDialogHeader className="items-center text-center">
                    <HelpCircle className="h-14 w-14 text-primary"/>
                    <AlertDialogTitle className="text-lg">
                        {t('LOGIN_REQUIRED_TITLE')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="mt-4">
                        {t('LOGIN_REQUIRED_DESCRIPTION')}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center">
                    <AlertDialogCancel className="rounded-xl">{t('CANCEL')}</AlertDialogCancel>
                    <AlertDialogAction onClick={login} className="rounded-xl">
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
        <Card className="rounded-2xl border border-border bg-background shadow-none">
            <CardHeader className="pb-4">
                <p className="text-xs tracking-[0.22em] uppercase text-muted-foreground">
                    Summary
                </p>
                <CardTitle className="text-sm font-semibold tracking-[0.18em] uppercase">
                    {t('CART_DETAILS')}
                </CardTitle>
            </CardHeader>

            <CardContent>
                <ScrollArea className="h-96">
                    <CartProductList storeContext={storeContext} cart={cart}/>
                </ScrollArea>
            </CardContent>

            <CardFooter className="flex-col items-stretch gap-4">
                <Separator/>
                {cart && (
                    <div className="flex items-center justify-between text-sm font-medium text-foreground">
                        <p>{t('SUB_TOTAL')}</p>
                        <p className="tabular-nums">{cart.displaySubTotal}</p>
                    </div>
                )}
            </CardFooter>
        </Card>
    )
}