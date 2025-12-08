'use client'
import {HelpCircle} from "lucide-react";
import {Box} from "@/types/content";
import {parseDescription} from "@/services/description-view-util";
import {useTranslations} from "next-intl";
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
import {Button} from "@/components/ui/button";

export const CheckoutAgreementDialog = ({box, isOpen, setIsOpen, isAgree, setIsAgree}: {
    box: Box | undefined
    isOpen: boolean,
    setIsOpen: (x: boolean) => void,
    isAgree: boolean,
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