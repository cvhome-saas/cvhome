import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {Secured} from '@/shell/auth/secured';
import {loadPageContext} from '@/shell/loaders/page-context';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('TITLE'), robots: {index: false}};
}

export function customerPage(theme: ThemeDefinition) {
    return async function CustomerPage() {
        const ctx = await loadPageContext(theme);
        return (
            <Secured storeContext={ctx.storeContext}>
                <theme.pages.Customer ctx={ctx} data={{}}/>
            </Secured>
        );
    };
}
