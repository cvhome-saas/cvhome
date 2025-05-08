import {CheckoutPageParams} from "@/types/params";
import {Breadcrumb} from "@/app/_themes/DEFAULT/componantes/Breadcrumb/Breadcrumb";
import {CheckoutForm} from "@/app/_themes/DEFAULT/componantes/Checkout/CheckoutForm";
import {CheckoutCartBox} from "@/app/_themes/DEFAULT/componantes/Checkout/CheckoutCartBox";
import {getTranslations} from "next-intl/server";
import {BreadcrumbItem} from "@/types/bread-crumb";

export default async function Page({params}: { params: CheckoutPageParams }) {
    const t = await getTranslations('PAGE.CHECKOUT');
    const current: BreadcrumbItem = {id: "0", name: t('TITLE'), href: `/checkout`};
    return <div className="flex-grow bg-background">
        <div className="p-6">
            <Breadcrumb breadcrumbs={{
                prev: [
                    {id: "1", name: t('HOME_TITLE'), href: '/'},
                ],
                current: current
            }}/>
            <div className="max-w-7xl mx-auto grid grid-cols-1 md:grid-cols-12 gap-5 pt-10">
                {/* User Form */}
                <CheckoutForm storeContext={params.storeContext}/>

                {/* Cart Details */}
                <div className="p-6 rounded-lg shadow-md col-span-4 sticky top-5 self-start border-2 border-border">
                    <CheckoutCartBox storeContext={params.storeContext}/>
                </div>
            </div>
        </div>
    </div>;

}