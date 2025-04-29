import {DefaultParams, StoreParams} from "@/types/params";
import {ComponentType} from "react";
import dynamic from "next/dynamic";
import {FOOTER_PATH, HEADER_PATH} from "@/types/constant";
import {getDirection, isRtl} from "@/utils/direction-utils";
import {NextIntlClientProvider} from "next-intl";
import {ColorSchema, ColorTheme, getThemeColors} from "@/types/color-schema";
import {toRootStyle} from "@/utils/color-utils";

export const StoreLayout = ({
                                p,
                                children,
                            }: {
    p: DefaultParams;
    children: React.ReactNode;
}) => {
    const Header: ComponentType<{
        params: StoreParams;
    }> = dynamic(() => import(`@/app/_themes/${p.store.theme}/${HEADER_PATH}`), {
        ssr: true,
    });

    const Footer: ComponentType<{
        params: StoreParams;
    }> = dynamic(() => import(`@/app/_themes/${p.store.theme}/${FOOTER_PATH}`), {
        ssr: true,
    });
    const colors: ColorSchema = p.store.colorTheme ? getThemeColors(p.store.colorTheme) : getThemeColors(ColorTheme.RAINBOW);

    const dir = getDirection(p.locale);

    return (
        <html lang={p.locale} dir={dir}>
        <head>
            <title>{p.store.name}</title>
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
        <body className={`flex flex-col min-h-screen ${isRtl(p.locale) ? 'rtl' : 'ltr'}`}>
        <NextIntlClientProvider>
            <Header params={p}/>
            {children}
            <Footer params={p}/>
        </NextIntlClientProvider>
        </body>
        </html>
    );
};