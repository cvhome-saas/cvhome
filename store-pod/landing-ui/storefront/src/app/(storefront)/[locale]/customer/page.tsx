import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadPageContext} from '@/shell/loaders/page-context';
import {Secured} from '@/shell/auth/secured';

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.CUSTOMER');
    return {title: t('TITLE'), robots: {index: false}};
}

export default async function CustomerPage() {
    const [theme, ctx] = await Promise.all([getTheme(), loadPageContext()]);
    return (
        <Secured storeContext={ctx.storeContext}>
            <theme.pages.Customer ctx={ctx} data={{}}/>
        </Secured>
    );
}
