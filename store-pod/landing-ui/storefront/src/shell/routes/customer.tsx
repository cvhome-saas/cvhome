import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {Secured} from '@/shell/auth/secured';
import {themed, type ThemeSource} from './theme-source';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('TITLE'), robots: {index: false}};
}

export function customerPage(theme: ThemeSource) {
    return async function CustomerPage() {
        const {theme: resolved, ctx} = await themed(theme);
        return (
            <Secured storeContext={ctx.storeContext}>
                <resolved.pages.Customer ctx={ctx} data={{}}/>
            </Secured>
        );
    };
}
