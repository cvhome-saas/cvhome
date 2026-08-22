'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData} from '@store-front/theme';

/**
 * The catalogue's thumb-index: one tab per top-level category (with its product count) plus the CMS pages
 * flagged for the menu, in a ruled strip under the masthead. The current section is a primary field.
 * Scrolls horizontally on narrow screens; sub-categories get their own sub-index on the category page.
 */
export function IndexStrip({data}: { data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    const categories = data.categories.filter(c => c.description && c.visible !== false);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    if (categories.length === 0 && pages.length === 0) return null;
    // the tree endpoint counts only a node's own products; a parent with none of its own shows its children's sum
    const countOf = (c: LayoutData['categories'][number]): number => c.productCount || (c.children ?? []).reduce((n, ch) => n + countOf(ch), 0);
    const active = (href: string) => href === '/' ? pathname === '/' : pathname === href || pathname.startsWith(`${href}/`);
    const tab = (href: string, label: string, count?: number) => (
        <Link prefetch={false} href={href} className="index-tab" aria-current={active(href) ? 'page' : undefined}>
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && count > 0 && <span className="count">{count}</span>}
        </Link>
    );
    return (
        <nav aria-label={t('INDEX')} className="border-b bg-background">
            <div className="mx-auto max-w-content px-gutter">
                <ul className="scroll-x scroll-x-fade -mx-gutter flex px-gutter lg:mx-0 lg:px-0">
                    <li className="flex shrink-0 border-s">{tab('/', tc('HOME'))}</li>
                    {categories.map(c => (
                        <li key={c.code} className="flex shrink-0">{tab(`/category/${c.description.friendlyUrl}`, c.description.name, countOf(c))}</li>
                    ))}
                    {pages.map((p, i) => (
                        <li key={p.code} className={i === 0 ? 'ms-auto flex shrink-0 border-s' : 'flex shrink-0'}>{tab(`/content/${p.description.friendlyUrl}`, p.description.name)}</li>
                    ))}
                </ul>
            </div>
        </nav>
    );
}
