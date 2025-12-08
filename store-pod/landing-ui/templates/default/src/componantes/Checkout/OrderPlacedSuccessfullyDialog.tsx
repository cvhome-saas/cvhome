'use client'

import {CheckCircle} from "lucide-react";
import {Order} from "@/types/order";
import {useRouter} from "@/i18n/navigation";
import {useTranslations} from "next-intl";
import {
    AlertDialog,
    AlertDialogAction,
    AlertDialogContent,
    AlertDialogDescription,
    AlertDialogFooter,
    AlertDialogHeader,
    AlertDialogTitle
} from "@/components/ui/alert-dialog";

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
                    <AlertDialogDescription className="space-y-2 !mt-4">
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