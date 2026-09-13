import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {loadFaq} from '@/shell/loaders/faq';
import {pageMetadata} from '@/shell/seo/metadata';
import {themed, type ThemeSource} from './theme-source';

type Props = { searchParams: Promise<{ group?: string }> };

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.FAQ');
    return pageMetadata(t('TITLE'));
}

export function helpPage(theme: ThemeSource) {
    return async function HelpPage({searchParams}: Props) {
        const {group} = await searchParams;
        const [{theme: resolved, ctx}, data] = await Promise.all([themed(theme), loadFaq(group)]);
        return (
            <>
                {data.faq.jsonLd && <script type="application/ld+json" dangerouslySetInnerHTML={{__html: data.faq.jsonLd}}/>}
                <resolved.pages.Faq ctx={ctx} data={data}/>
            </>
        );
    };
}
