import "./globals.css";
import {DefaultParams, LocaleParam} from "@/types/params";
import {StoreService} from "@/services/store-service";
import {extractSsrContext} from "@/utils/store-context-ssr-utils";
import {Store} from "@/types/store";
import {StoreNotFoundLayout} from "@/layout/StoreNotFoundLayout";
import {StoreLayout} from "@/layout/StoreLayout";
import {Metadata} from "next";
import {headers} from 'next/headers';
import {ContentService} from "@/services/content-service";
import {redirectToSupportedLang} from "@/utils/locale-utils";

const svgIcon = `<svg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 100 100%22><text y=%22.9em%22 font-size=%2290%22>🦊</text></svg>`;

export async function generateMetadata(
    {params}: { params: Promise<LocaleParam> }
): Promise<Metadata> {
    const storeContext = await extractSsrContext();
    const metaTitle = await ContentService.getBox(storeContext, 'meta-title');
    const metaDescription = await ContentService.getBox(storeContext, 'meta-description');

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
    params: Promise<LocaleParam>;
}) {
    const aparams = await params;
    const urlLocale = aparams.locale;
    const storeContext = await extractSsrContext();
    const store: Store | undefined = storeContext.store !== "" ? await StoreService.getStore(storeContext) : undefined;

    const defaultParams: DefaultParams = {
        locale: urlLocale,
        store: store as unknown as Store,
        storeContext: storeContext
    };
    if (store) {
        const storeSupportedLanguages = store.supportedLanguages || [];

        if (!storeSupportedLanguages.includes(urlLocale)) {
            redirectToSupportedLang(store, await headers(), urlLocale);
        } else {
            return (
                <StoreLayout p={defaultParams}>
                    {children}
                </StoreLayout>
            );
        }
    } else {
        return <StoreNotFoundLayout p={defaultParams}/>;
    }
}
