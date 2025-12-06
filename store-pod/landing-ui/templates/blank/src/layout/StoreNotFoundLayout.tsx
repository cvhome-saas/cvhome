import {DefaultParams} from "@/types/params";
import {NextIntlClientProvider} from "next-intl";

import Head from "next/head";

export const StoreNotFoundLayout = ({p}: { p: DefaultParams }) => {
    return (
        <html lang={p.locale}>
        <Head>
            <title>store not found</title>
        </Head>
        <body>
        <NextIntlClientProvider>
            <div className="flex flex-col items-center justify-center min-h-screen bg-white px-6">
                <div className="max-w-md text-center">
                    <h1 className="text-4xl font-bold text-red-600 mb-4">Store Not Found</h1>
                    <p className="text-gray-700 text-lg">
                        We couldn’t find the store you’re looking for. It might not exist or could be temporarily
                        unavailable.
                    </p>
                </div>
            </div>
        </NextIntlClientProvider>
        </body>
        </html>
    )
}