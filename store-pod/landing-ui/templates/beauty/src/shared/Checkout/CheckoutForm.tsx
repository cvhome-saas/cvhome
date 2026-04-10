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
                onSubmit={handleSubmit(onSubmit)} noValidate
                className="p-8 rounded-3xl shadow-xl shadow-primary/5 col-span-8 border border-primary/10 bg-card/50 backdrop-blur-sm"
            >
                <h2 className="text-3xl font-serif font-bold mb-8 text-foreground/90">
                    {t('FORM_TITLE')}
                </h2>
                <div className="space-y-8">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                            <Label htmlFor="firstName" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('FIRST_NAME')}
                            </Label>
                            <Input
                                id="firstName"
                                {...register("customer.billing.firstName")}
                                type="text"
                                placeholder={t('FIRST_NAME')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.billing?.firstName}
                            />
                            {errors?.customer?.billing?.firstName && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.firstName.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="lastName" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('LAST_NAME')}
                            </Label>
                            <Input
                                id="lastName"
                                {...register("customer.billing.lastName")}
                                type="text"
                                placeholder={t('LAST_NAME')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.billing?.lastName}
                            />
                            {errors?.customer?.billing?.lastName && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.lastName.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="space-y-2">
                            <Label htmlFor="emailAddress" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('EMAIL')}
                            </Label>
                            <Input
                                id="emailAddress"
                                {...register("customer.emailAddress")}
                                type="email"
                                placeholder={t('EMAIL')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.emailAddress}
                            />
                            {errors?.customer?.emailAddress && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.emailAddress.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="phone" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('PHONE')}
                            </Label>
                            <Input
                                id="phone"
                                {...register("customer.billing.phone")}
                                type="tel"
                                placeholder={t('PHONE')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.billing?.phone}
                            />
                            {errors?.customer?.billing?.phone && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.phone.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="space-y-2">
                            <Label htmlFor="country" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('COUNTRY')}
                            </Label>
                            <Select onValueChange={(value) => setValue("customer.billing.country", value)}
                                    defaultValue={defaultCheckoutValue.customer.billing.country}>
                                <SelectTrigger id="country" className="rounded-full border-primary/10 focus:ring-primary/20" aria-invalid={!!errors.customer?.billing?.country}>
                                    <SelectValue placeholder={t('SELECT_COUNTRY')}/>
                                </SelectTrigger>
                                <SelectContent className="rounded-2xl border-primary/10">
                                    {readableCountryList?.map((country) => (
                                        <SelectItem key={country.code} value={country.code}>
                                            {country.name}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                            {errors?.customer?.billing?.country && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.country.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="city" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('CITY')}
                            </Label>
                            <Input
                                id="city"
                                {...register("customer.billing.city")}
                                type="text"
                                placeholder={t('CITY')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.billing?.city}
                            />
                            {errors?.customer?.billing?.city && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.city.message}</p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="postalCode" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                                {t('POSTAL_CODE')}
                            </Label>
                            <Input
                                id="postalCode"
                                {...register("customer.billing.postalCode")}
                                type="text"
                                placeholder={t('POSTAL_CODE')}
                                className="rounded-full border-primary/10 focus-visible:ring-primary/20"
                                aria-invalid={!!errors.customer?.billing?.postalCode}
                            />
                            {errors?.customer?.billing?.postalCode && (
                                <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.postalCode.message}</p>
                            )}
                        </div>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="address" className="text-xs uppercase tracking-widest font-semibold text-muted-foreground">
                            {t('ADDRESS')}
                        </Label>
                        <Textarea
                            id="address"
                            {...register("customer.billing.address")}
                            placeholder={t('ADDRESS')}
                            className="rounded-3xl border-primary/10 focus-visible:ring-primary/20 min-h-[120px] resize-none"
                            aria-invalid={!!errors.customer?.billing?.address}
                        />
                        {errors?.customer?.billing?.address && (
                            <p className="text-xs font-medium text-destructive mt-1">{errors.customer.billing.address.message}</p>
                        )}
                    </div>
                </div>
                {
                    agreement && (
                        <div className="mt-8 flex items-center p-4 rounded-2xl bg-primary/5 border border-primary/10">
                            <Checkbox
                                onClick={handleClickOnAgreement}
                                id="isAgree"
                                {...register("customer.billing.isAgree")}
                                checked={isAgree}
                                onCheckedChange={(checked) => setIsAgree(!!checked)}
                                className="rounded-md border-primary/30 data-[state=checked]:bg-primary data-[state=checked]:border-primary"
                                aria-invalid={!!errors.customer?.billing?.isAgree}
                            />
                            <Label htmlFor="isAgree" className="ms-3 text-sm font-medium text-foreground/80 cursor-pointer">
                                {t('AGREEMENT')}
                            </Label>
                            {errors?.customer?.billing?.isAgree && !isAgree && (
                                <p className="text-xs font-medium text-destructive ms-3">{errors.customer.billing.isAgree.message}</p>
                            )}
                        </div>
                    )
                }

                <div className="mt-8 pt-6 border-t border-primary/10">
                    <div className="flex justify-center">
                        <Button
                            type="submit"
                            size="lg"
                            className="rounded-full px-12 bg-primary hover:bg-primary/90 text-primary-foreground shadow-lg shadow-primary/20 transition-all active:scale-95"
                        >
                            {t('PLACE_ORDER')}
                        </Button>
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
            <AlertDialogContent className="rounded-3xl border-primary/10 bg-card/95 backdrop-blur-md">
                <AlertDialogHeader>
                    <div className="flex justify-center mb-2">
                        <div className="p-4 rounded-full bg-primary/10">
                            <HelpCircle className="h-12 w-12 text-primary"/>
                        </div>
                    </div>
                    <AlertDialogTitle className="text-center font-serif text-2xl">
                        {t('TERMS_AND_CONDITIONS')}
                    </AlertDialogTitle>
                    {box && <AlertDialogDescription
                        className="max-h-60 overflow-y-auto text-foreground/80 font-light leading-relaxed p-4 bg-secondary/20 rounded-2xl"
                        dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                    }
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center gap-4">
                    <AlertDialogCancel asChild>
                        <Button variant="ghost" className="rounded-full px-8 text-muted-foreground hover:bg-secondary/50"
                                onClick={handleReject}>{t('REJECT')}</Button>
                    </AlertDialogCancel>
                    <AlertDialogAction asChild>
                        <Button className="rounded-full px-8 bg-primary hover:bg-primary/90" onClick={handleAgree}>{t('AGREE')}</Button>
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
            <AlertDialogContent className="rounded-3xl border-primary/10 bg-card/95 backdrop-blur-md">
                <AlertDialogHeader className="items-center text-center">
                    <div className="p-4 rounded-full bg-primary/5 mb-4">
                        <CheckCircle className="h-16 w-16 text-primary"/>
                    </div>
                    <AlertDialogTitle className="text-2xl font-serif">
                        {t('ORDER_PLACED_SUCCESSFULLY')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="space-y-3 mt-4 text-foreground/70">
                        {order ? (
                            <>
                                <p className="font-medium text-lg text-primary">
                                    {t('ORDER_PLACED_SUCCESSFULLY')}
                                </p>
                                <div className="inline-block px-4 py-2 rounded-full bg-secondary/30 border border-primary/5">
                                    <strong>{t('ORDER_ID')}:</strong> <span className="font-mono text-primary/80">{order.id}</span>
                                </div>
                            </>
                        ) : (
                            <p>{t('NO_ORDER_DETAILED')}</p>
                        )}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center mt-6">
                    <AlertDialogAction onClick={handleContinueShopping} className="w-full sm:w-auto rounded-full px-12 bg-primary hover:bg-primary/90 shadow-lg shadow-primary/10">
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
            <AlertDialogContent className="rounded-3xl border-primary/10 bg-card/95 backdrop-blur-md">
                <AlertDialogHeader className="items-center text-center">
                    <div className="p-4 rounded-full bg-primary/10 mb-2">
                        <HelpCircle className="h-12 w-12 text-primary"/>
                    </div>
                    <AlertDialogTitle className="text-2xl font-serif">
                        {t('LOGIN_REQUIRED_TITLE')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="mt-4 text-foreground/70">
                        {t('LOGIN_REQUIRED_DESCRIPTION')}
                    </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter className="sm:justify-center gap-4 mt-6">
                    <AlertDialogCancel className="rounded-full px-8 hover:bg-secondary/50 border-primary/10">{t('CANCEL')}</AlertDialogCancel>
                    <AlertDialogAction onClick={login} className="rounded-full px-8 bg-primary hover:bg-primary/90 shadow-lg shadow-primary/10">
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
        <Card className="rounded-3xl border-primary/10 shadow-xl shadow-primary/5 bg-card/50 backdrop-blur-sm overflow-hidden">
            <CardHeader className="bg-primary/5 border-b border-primary/5 pb-4">
                <CardTitle className="font-serif text-xl text-primary">{t('CART_DETAILS')}</CardTitle>
            </CardHeader>
            <CardContent className="pt-6">
                <ScrollArea className="h-96 pr-4">
                    <CartProductList storeContext={storeContext} cart={cart}/>
                </ScrollArea>
            </CardContent>
            <CardFooter className="flex-col items-start bg-primary/5 p-6 space-y-4">
                <Separator className="bg-primary/10"/>
                {cart && <>
                    <div className="w-full space-y-2">
                        <div className="flex justify-between items-center text-lg font-serif">
                            <p className="text-muted-foreground">{t('SUB_TOTAL')}</p>
                            <p className="font-bold text-primary">{cart.displaySubTotal}</p>
                        </div>
                        <p className="text-[10px] uppercase tracking-[0.2em] text-center text-muted-foreground pt-4">
                            Premium Beauty Selection
                        </p>
                    </div>
                </>}
            </CardFooter>
        </Card>
    )
}