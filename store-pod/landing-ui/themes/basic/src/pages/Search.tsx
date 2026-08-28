import {getTranslations} from 'next-intl/server';
import type {PageProps, SearchData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {SearchNotice, SearchResults} from '../sections/SearchResults';

/**
 * What the shop found: the query as a running head, the count beneath it, then the ruled listing — the same
 * furniture as a category, because results are a section of the catalogue like any other.
 */
export async function Search({ctx, data}: PageProps<SearchData>) {
    const t = await getTranslations('PAGE.SEARCH');
    const term = data.query.q.trim();
    return (
        <PageShell width={ctx.layout.container} className="flex flex-col gap-6 py-6 lg:py-8">
            <header className="flex flex-col gap-2">
                <div className="running-head">
                    <h1 className="display text-4xl lg:text-5xl">
                        <bdi dir="auto">{term ? t('HEADING', {query: term}) : t('HEADING_EMPTY_QUERY')}</bdi>
                    </h1>
                </div>
                <SearchNotice didYouMean={data.didYouMean} fallbackLanguage={data.fallbackLanguage}/>
            </header>
            <SearchResults storeContext={ctx.storeContext} data={data} grid={ctx.layout.productGrid}/>
        </PageShell>
    );
}
