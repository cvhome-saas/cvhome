import {DefaultParams, LayoutParams} from "@/types/params";
import {getDirection, isRtl} from "@/services/direction-utils";
import {NextIntlClientProvider} from "next-intl";
import {ColorSchema, ColorTheme, getThemeColors} from "@/types/color-schema";
import Header from "@/layout/Header";
import Footer from "@/layout/Footer";
import {toRootStyle} from "@/services/color-utils";

export const StoreLayout = ({p, children,}: {
    p: DefaultParams;
    children: React.ReactNode;
}) => {
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
            <Header params={p as unknown as LayoutParams}/>
            {children}
            <Footer params={p as unknown as LayoutParams}/>
        </NextIntlClientProvider>
        </body>
        </html>
    );
};