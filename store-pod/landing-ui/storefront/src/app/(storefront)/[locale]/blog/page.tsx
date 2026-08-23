import type {Metadata} from 'next';
import {getTranslations} from 'next-intl/server';
import {getTheme} from '@/shell/theme/get-theme';
import {loadBlogIndex} from '@/shell/loaders/blog';
import {loadPageContext} from '@/shell/loaders/page-context';
import {pageMetadata} from '@/shell/seo/metadata';

type Props = { searchParams: Promise<{ page?: string; category?: string; tag?: string }> };

export async function generateMetadata(): Promise<Metadata> {
    const t = await getTranslations('PAGE.BLOG');
    return pageMetadata(t('TITLE'));
}

export default async function BlogIndexPage({searchParams}: Props) {
    const q = await searchParams;
    const page = Math.max(0, Number(q.page ?? 0) || 0);
    const [theme, ctx, data] = await Promise.all([getTheme(), loadPageContext(), loadBlogIndex({page, category: q.category, tag: q.tag})]);
    return <theme.pages.BlogIndex ctx={ctx} data={data}/>;
}
