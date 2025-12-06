import {Cart} from "@/types/cart";
import {useTranslations} from "next-intl";

export const CartSummary = ({cart}: {
    cart: Cart | undefined,
}) => {
    const t = useTranslations('COMPONENTS.CART');

    if (!cart) {
        return null;
    }

    return (
        <div className="space-y-2 border-t pt-6">
            <div className="flex justify-between text-base font-medium text-foreground">
                <p>{t('SUB_TOTAL')}</p>
                <p>{cart.displaySubTotal}</p>
            </div>
            {/* You can add other summary lines here, like discounts, shipping, etc. */}
        </div>
    );
}