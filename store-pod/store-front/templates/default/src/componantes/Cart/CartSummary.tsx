import {Cart} from "@/types/cart";
import {useTranslations} from "next-intl";

export const CartSummary = ({cart}: {
    cart: Cart | undefined,
}) => {
    const t = useTranslations('COMPONENTS.CART');
    return (
        cart && <div className="space-y-2">
            <div className="flex justify-between text-neutral">
                <span>{t('SUB_TOTAL')}:</span>
                <span>{cart.displaySubTotal}</span>
            </div>
        </div>
    );
}