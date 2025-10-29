import {Dialog, DialogPanel, DialogTitle, Transition, TransitionChild} from "@headlessui/react";
import {CheckCircleIcon} from "@heroicons/react/24/solid";
import {Order} from "@/types/order";
import {Fragment} from "react";
import {useRouter} from "@/i18n/navigation";
import {useTranslations} from "next-intl";

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
        <Transition appear show={isOpen} as={Fragment}>
            <Dialog as="div" className="relative z-10" onClose={() => handleContinueShopping()}>
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
                                <div className="flex flex-col items-center">
                                    <CheckCircleIcon className="h-16 w-16 text-success"/>
                                    <DialogTitle
                                        as="h3"
                                        className="mt-4 text-lg font-medium leading-6 text-foreground"
                                    >
                                        {t('ORDER_PLACED_SUCCESSFULLY')}
                                    </DialogTitle>
                                </div>
                                <div className="mt-4 space-y-4 text-center">
                                    {order ? (
                                        <>
                                            <p className="text-sm text-neutral">
                                                {t('ORDER_PLACED_SUCCESSFULLY')}
                                            </p>
                                            <p className="text-sm text-neutral">
                                                <strong>{t('ORDER_ID')}:</strong> {order.id}
                                            </p>
                                        </>
                                    ) : (
                                        <p className="text-sm text-neutral">{t('NO_ORDER_DETAILED')}</p>
                                    )}
                                </div>

                                <div className="mt-6 flex justify-center">
                                    <button
                                        type="button"
                                        className="px-4 py-2 text-sm font-medium text-foreground bg-error border border-transparent rounded-md hover:bg-hover-error focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-error"
                                        onClick={handleContinueShopping}
                                    >
                                        {t('CONTINUE_SHOPPING')}
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