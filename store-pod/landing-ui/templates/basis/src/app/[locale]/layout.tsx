import "./globals.css";
import {LayoutParams, LocaleParam} from "@/types/params";
import {StoreService} from "@/services/store-service";
import {extractSsrContext} from "@/services/store-context-ssr-utils";
import {Store} from "@/types/store";
import {Metadata} from "next";
import {headers} from 'next/headers';
import {ContentService} from "@/services/content-service";
import {localSupported, redirectToSupportedLang} from "@/services/locale-utils";
import {ColorSchema, ColorTheme, getThemeColors} from "@/types/color-schema";
import {getDirection, isRtl} from "@/services/direction-utils";
import {toRootStyle} from "@/services/color-utils";
import {NextIntlClientProvider} from "next-intl";
import * as React from "react";
import {CategoryService} from "@/services/category-service";
import {Box} from "@/types/content";
import {Header} from "@/shared/Layout/Header";
import {Footer} from "@/shared/Layout/Footer";

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

    if (!store)
        return null;

    
    if (!localSupported(urlLocale,store)) {
        redirectToSupportedLang(store, await headers(), urlLocale);
    } else {

        const defaultParams: LayoutParams = {
            locale: urlLocale,
            store: store as unknown as Store,
            storeContext: storeContext,
            categories: await CategoryService.getCategories(storeContext),
            contents: await ContentService.getContents(storeContext),
            cart: undefined
        };
        
        const dir = getDirection(defaultParams.locale);

        const headerBox: Box | undefined = await ContentService.getBox(defaultParams.storeContext, "header-message");

        const colors: ColorSchema = store.colorTheme ? getThemeColors(store.colorTheme) : getThemeColors(ColorTheme.RAINBOW);
        return (
            <html lang={defaultParams.locale} dir={dir}>
            <head>
                <title>{defaultParams.store.name}</title>
                <style
                    dangerouslySetInnerHTML={{
                        __html: `
                            :root {
                                ${toRootStyle(colors)}
                            }
                        `,
                    }}
                />
            </head>
            <body className={`flex flex-col min-h-screen ${isRtl(defaultParams.locale) ? 'rtl' : 'ltr'}`}>
            <NextIntlClientProvider>
                <Header params={defaultParams} headerBox={headerBox}/>
                {children}
                <Footer params={defaultParams}/>
            </NextIntlClientProvider>
            </body>
            </html>
        );
    }

}
