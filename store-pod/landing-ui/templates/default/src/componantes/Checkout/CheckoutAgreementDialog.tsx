'use client'
import {Dialog, DialogPanel, DialogTitle, Transition, TransitionChild} from "@headlessui/react";
import {QuestionMarkCircleIcon, XMarkIcon} from "@heroicons/react/24/solid";
import {Fragment} from "react";
import {Box} from "@/types/content";
import {parseDescription} from "@/services/description-view-util";
import {useTranslations} from "next-intl";

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
        <Transition appear show={isOpen} as={Fragment}>
            <Dialog
                as="div"
                className="relative z-10"
                onClose={() => setIsOpen(false)}
                aria-labelledby="dialog-title"
                aria-describedby="dialog-description"
            >
                <div className="fixed inset-0 overflow-y-auto">
                    <div className="flex items-center justify-center min-h-full p-4 text-center">
                        <TransitionChild
                            as={Fragment}
                            enter="ease-out duration-300"
                            enterFrom="opacity-0 scale-95"
                            enterTo="opacity-100 scale-100"
                            leave="ease-in duration-200"
                            leaveFrom="opacity-100 scale-100"
                            leaveTo="opacity-0 scale-95"
                        >
                            <DialogPanel
                                className="w-full max-w-md p-6 overflow-hidden text-start align-middle transition-all transform bg-background shadow-xl rounded-2xl">
                                <div className="flex justify-between items-center">
                                    <QuestionMarkCircleIcon className="h-16 w-16 text-primary"/>
                                    <button
                                        onClick={() => setIsOpen(false)}
                                        className="text-neutral hover:text-foreground transition duration-200"
                                        aria-label="Close"
                                    >
                                        <XMarkIcon className="h-6 w-6"/>
                                    </button>
                                </div>
                                <DialogTitle
                                    as="h3"
                                    id="dialog-title"
                                    className="mt-4 text-lg font-medium leading-6 text-foreground text-center"
                                >
                                    {t('TERMS_AND_CONDITIONS')}
                                </DialogTitle>
                                <div
                                    id="dialog-description"
                                    className="mt-2 text-sm text-neutral"
                                >
                                    {
                                        box &&
                                        <div dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                                    }
                                </div>
                                <div className="mt-4 flex justify-center space-x-4">
                                    <button
                                        onClick={handleAgree}
                                        className="bg-success text-foreground font-bold py-2 px-4 rounded-lg hover:bg-hover-success transition duration-200"
                                    >
                                        {t('AGREE')}
                                    </button>
                                    <button
                                        onClick={handleReject}
                                        className="bg-error text-foreground font-bold py-2 px-4 rounded-lg hover:bg-hover-error transition duration-200"
                                    >
                                        {t('REJECT')}
                                    </button>
                                </div>
                            </DialogPanel>
                        </TransitionChild>
                    </div>
                </div>
            </Dialog>
        </Transition>
    );
};