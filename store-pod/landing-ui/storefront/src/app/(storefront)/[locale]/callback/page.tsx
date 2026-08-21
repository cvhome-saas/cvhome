import type {Metadata} from 'next';
import {Suspense} from 'react';
import {getStoreContext} from '@/shell/request/store-context';
import {CallbackClient} from '@/shell/auth/callback-client';

export const metadata: Metadata = {robots: {index: false}};

export default async function CallbackPage() {
    const storeContext = await getStoreContext();
    return (
        <Suspense fallback={null}>
            <CallbackClient storeContext={storeContext}/>
        </Suspense>
    );
}
