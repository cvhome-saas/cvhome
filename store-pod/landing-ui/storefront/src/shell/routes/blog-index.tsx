import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import type {ThemeDefinition} from '@store-front/theme';
import {loadBlogIndex} from '@/shell/loaders/blog';
import {pageMetadata} from '@/shell/seo/metadata';
import {loadPageContext} from '@/shell/loaders/page-context';

type Props = { searchParams: Promise<{ page?: string; category?: string; tag?: string }> };

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.BLOG');
    return pageMetadata(t('TITLE'));
}

export function blogIndexPage(theme: ThemeDefinition) {
    return async function BlogIndexPage({searchParams}: Props) {
        const q = await searchParams;
        const page = Math.max(0, Number(q.page ?? 0) || 0);
        const [ctx, data] = await Promise.all([
            loadPageContext(theme), loadBlogIndex({page, category: q.category, tag: q.tag}),
        ]);
        return <theme.pages.BlogIndex ctx={ctx} data={data}/>;
    };
}
