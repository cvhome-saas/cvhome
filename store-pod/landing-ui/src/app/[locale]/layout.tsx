import "./globals.css";
import {DefaultParams} from "@/types/params";
import {StoreService} from "@/services/store-service";
import {extractSsrContext} from "@/utils/store-context-ssr-utils";
import {Store} from "@/types/store";
import {StoreNotFoundLayout} from "@/app/[locale]/_layout/StoreNotFoundLayout";
import {StoreLayout} from "@/app/[locale]/_layout/StoreLayout";
import {Metadata} from "next";
import {redirect} from 'next/navigation';
import {headers} from 'next/headers';
import {buildDefaultLangRedirectionUrl} from "@/utils/locale-utils";
import {ContentService} from "@/services/content-service";

const svgIcon = `<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🦊</text></svg>`;

export async function generateMetadata(
    {params}: { params: Promise<DefaultParams> }
): Promise<Metadata> {

    const p = await params;
    p.storeContext = await extractSsrContext();
    const metaTitle = await ContentService.getBox(p.storeContext, 'meta-title');
    const metaDescription = await ContentService.getBox(p.storeContext, 'meta-description');

    return {
        title: metaTitle?.description.description || '',
        description: metaDescription?.description.description || '',
        icons: {
            icon: `data:image/svg+xml,${svgIcon}`,
        },
    };
}

export default async function LocaleLayout({children, params}: {
    children: React.ReactNode;
    params: Promise<DefaultParams>;
}) {
    const p = await params;
    const urlLocale = p.locale;
    p.storeContext = await extractSsrContext();
    const store: Store | undefined = p.storeContext.store !== "" ? await StoreService.getStore(p.storeContext) : undefined;

    if (store) {
        p.store = store;
        const storeSupportedLanguages = store.supportedLanguages || [];

        if (storeSupportedLanguages.includes(urlLocale)) {
            return <StoreLayout p={p} children={children}/>;
        } else {
            redirect(buildDefaultLangRedirectionUrl(store, await headers(), urlLocale));
        }
    } else {
        return <StoreNotFoundLayout p={p}/>;
    }
}


