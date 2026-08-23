import type {Metadata} from 'next';
import {getStoreContext} from '@/shell/request/store-context';
import {LoginRedirect} from '@/shell/auth/login-redirect';

export const metadata: Metadata = {robots: {index: false}};

export default async function LoginPage() {
    const storeContext = await getStoreContext();
    return <LoginRedirect storeContext={storeContext}/>;
}
