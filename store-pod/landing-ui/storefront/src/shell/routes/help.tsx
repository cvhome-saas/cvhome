import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {loadFaq} from '@/shell/loaders/faq';
import {pageMetadata} from '@/shell/seo/metadata';
import {loadPageContext} from '@/shell/loaders/page-context';

type Props = { searchParams: Promise<{ group?: string }> };

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.FAQ');
    return pageMetadata(t('TITLE'));
}

export function helpPage(theme: ThemeDefinition) {
    return async function HelpPage({searchParams}: Props) {
        const {group} = await searchParams;
        const [ctx, data] = await Promise.all([loadPageContext(theme), loadFaq(group)]);
        return (
            <>
                {data.faq.jsonLd && <script type="application/ld+json" dangerouslySetInnerHTML={{__html: data.faq.jsonLd}}/>}
                <theme.pages.Faq ctx={ctx} data={data}/>
            </>
        );
    };
}
