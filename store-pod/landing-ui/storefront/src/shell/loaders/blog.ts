import 'server-only';
import {cache} from 'react';
import {notFound, redirect} from 'next/navigation';
import {getTranslations} from 'next-intl/server';
import {ContentService} from '@store-front/services/content-service';
import {parseDescription} from '@store-front/services/description-view-util';
import {isApiError} from '@store-front/types';
import type {BlogIndexData, BlogPostData} from '@store-front/theme';
import {getStoreContext} from '@/shell/request/store-context';

export const loadBlogIndex = cache(async (q: { page?: number; category?: string; tag?: string }): Promise<BlogIndexData> => {
    const ctx = await getStoreContext();
    const [posts, t] = await Promise.all([
        ContentService.getPosts(ctx, {page: q.page ?? 0, count: 12, category: q.category, tag: q.tag}),
        getTranslations('COMMON'),
    ]);
    const tb = await getTranslations('PAGE.BLOG');
    const categories = (posts?.content ?? []).flatMap(p => p.categories)
        .filter((c, i, all) => all.findIndex(x => x.slug === c.slug) === i);
    return {
        posts: posts ?? {totalPages: 0, size: 0, totalElements: 0, pageNumber: 0, content: []},
        categories,
        category: q.category,
        tag: q.tag,
        breadcrumbs: [{id: 'home', name: t('HOME'), href: '/'}, {id: 'blog', name: tb('TITLE'), href: '/blog'}],
    };
});

export const loadBlogPost = cache(async (slug: string, preview?: string): Promise<BlogPostData> => {
    const ctx = await getStoreContext();
    let post;
    try {
        post = await ContentService.getPost(ctx, slug, preview);
    } catch (e) {
        if (isApiError(e) && e.category === 'NOT_FOUND') {
            const moved = await ContentService.getRedirect(ctx, `/blog/${slug}`);
            if (moved) redirect(`/${ctx.locale}${moved.to}`);
            notFound();
        }
        throw e;
    }
    const [t, tb] = await Promise.all([getTranslations('COMMON'), getTranslations('PAGE.BLOG')]);
    return {
        post,
        html: parseDescription({description: post.body ?? ''} as never),
        breadcrumbs: [
            {id: 'home', name: t('HOME'), href: '/'},
            {id: 'blog', name: tb('TITLE'), href: '/blog'},
            {id: String(post.id), name: post.title, href: `/blog/${post.slug}`},
        ],
    };
});
