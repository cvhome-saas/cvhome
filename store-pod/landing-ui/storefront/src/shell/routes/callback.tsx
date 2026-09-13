import type {Metadata} from 'next';
import {Suspense} from 'react';
import {getStoreContext} from '@/shell/request/store-context';
import {CallbackClient} from '@/shell/auth/callback-client';

export const metadata: Metadata = {robots: {index: false}};

/** cua's return from the authorization-code flow. Theme-agnostic: it only hands the code to the client. */
export default async function CallbackPage() {
    const storeContext = await getStoreContext();
    return (
        <Suspense fallback={null}>
            <CallbackClient storeContext={storeContext}/>
        </Suspense>
    );
}
