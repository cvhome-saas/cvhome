import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadFaq} from '@/shell/loaders/faq';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

type Props = { searchParams: Promise<{ group?: string }> };

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.FAQ');
    return pageMetadata(t('TITLE'));
}

export default async function HelpPage({searchParams}: Props) {
    const {group} = await searchParams;
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadFaq(group)]);
    return (
        <>
            {data.faq.jsonLd && <script type="application/ld+json" dangerouslySetInnerHTML={{__html: data.faq.jsonLd}}/>}
            <theme.pages.Faq ctx={ctx} data={data}/>
        </>
    );
}
