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
import {useCheckoutForm} from "@/hooks/use-checkout-form";

export const CheckoutForm = ({storeContext}: { storeContext: StoreContext }) => {
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
        agreement,
        order,
        isAgree,
        setIsAgree,
        readableCountryList,
        handleClickOnAgreement,
        onSubmit,
    } = useCheckoutForm(storeContext);

    return (
        <>
            <OrderPlacedSuccessfullyDialog order={order} isOpen={successDialogOpen} setIsOpen={setSuccessDialogOpen}/>
            <CheckoutAgreementDialog box={agreement} isOpen={agreeDialogOpen} setIsOpen={setAgreeDialogOpen}
                                     setIsAgree={setIsAgree}/>
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
            <AlertDialogContent>
                <AlertDialogHeader>
                    <div className="flex justify-center">
                        <HelpCircle className="h-16 w-16 text-primary"/>
                    </div>
                    <AlertDialogTitle className="text-center">
                        {t('TERMS_AND_CONDITIONS')}
                    </AlertDialogTitle>
                    {box && <AlertDialogDescription
                        className="max-h-60 overflow-y-auto text-foreground"
                        dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                    }
                </AlertDialogHeader>
                <AlertDialogFooter>
                    <AlertDialogCancel asChild>
                        <Button variant="destructive" className="text-destructive-foreground"
                                onClick={handleReject}>{t('REJECT')}</Button>
                    </AlertDialogCancel>
                    <AlertDialogAction asChild>
                        <Button onClick={handleAgree}>{t('AGREE')}</Button>
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
            <AlertDialogContent>
                <AlertDialogHeader className="items-center text-center">
                    <CheckCircle className="h-16 w-16 text-green-500"/>
                    <AlertDialogTitle className="text-lg">
                        {t('ORDER_PLACED_SUCCESSFULLY')}
                    </AlertDialogTitle>
                    <AlertDialogDescription className="space-y-2 mt-4!">
                        {order ? (
                            <>
                                <p>
                                    {t('ORDER_PLACED_SUCCESSFULLY')}
                                </p>
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
                    <AlertDialogAction onClick={handleContinueShopping} className="w-full">
                        {t('CONTINUE_SHOPPING')}
                    </AlertDialogAction>
                </AlertDialogFooter>
            </AlertDialogContent>
        </AlertDialog>
    );
};