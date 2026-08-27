import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {parseSearchQuery} from '@store-front/types';
import {getTheme} from '@/shell/theme/get-theme';
import {DefaultSearchPage} from '@/shell/theme/default-search-page';
import {loadSearch} from '@/shell/loaders/search';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

type Props = { searchParams: Promise<Record<string, string | string[] | undefined>> };

async function query(searchParams: Props['searchParams']) {
    const sp = await searchParams;
    const usp = new URLSearchParams();
    for (const [k, v] of Object.entries(sp)) if (typeof v === 'string') usp.set(k, v);
    return parseSearchQuery(usp);
}

/**
 * A results page is not a page anyone should reach from a search engine — it has no content of its own, and
 * every distinct query would be another thin URL competing with the category pages that do.
 */
export async function generateMetadata({searchParams}: Props): Promise<Metadata> {
    const t = await getTranslations('PAGE.SEARCH');
    const {q} = await query(searchParams);
    return {
        ...pageMetadata(q ? t('HEADING', {query: q}) : t('TITLE')),
        robots: {index: false, follow: true},
    };
}

export default async function SearchPage({searchParams}: Props) {
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadSearch(await query(searchParams))]);
    const Page = theme.pages.Search ?? DefaultSearchPage;
    return <Page ctx={ctx} data={data}/>;
}
