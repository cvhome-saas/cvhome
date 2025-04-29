import {DefaultParams} from "@/types/params";
import {NextIntlClientProvider} from "next-intl";
import {StoreNotFound} from "@/app/[locale]/_components/StoreNotFound";

export const StoreNotFoundLayout = ({p}: { p: DefaultParams }) => {
    return (
        <html lang={p.locale}>
        <head>
            <title>store not found</title>
        </head>
        <body>
        <NextIntlClientProvider>
            <StoreNotFound/>
        </NextIntlClientProvider>
        </body>
        </html>
    )
}