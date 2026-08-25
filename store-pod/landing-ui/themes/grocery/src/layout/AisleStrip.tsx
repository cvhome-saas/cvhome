'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData} from '@store-front/theme';

/**
 * The aisle boards: one tile per top-level category (with its product count) plus the CMS pages flagged
 * for the menu, hanging in a strip under the counter. The current aisle takes the price-board colour.
 * Scrolls horizontally on narrow screens; sub-categories get their own tile row on the category page.
 */
export function AisleStrip({data}: { data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    const categories = data.categories.filter(c => c.description && c.visible !== false);
    const pages = data.pages.filter(p => p.inMenu);
    // Main-menu entries that are not pages (blog, help, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    if (categories.length === 0 && pages.length === 0) return null;
    // the tree endpoint counts only a node's own products; a parent with none of its own shows its children's sum.
    // Stores whose hierarchy ships no counts at all (the demo seed does this) render plain tiles — never invented numbers.
    const countOf = (c: LayoutData['categories'][number]): number => c.productCount || (c.children ?? []).reduce((n, ch) => n + countOf(ch), 0);
    const active = (href: string) => href === '/' ? pathname === '/' : pathname === href || pathname.startsWith(`${href}/`);
    const tile = (href: string, label: string, count?: number) => (
        <Link prefetch={false} href={href} className="aisle-tile" aria-current={active(href) ? 'page' : undefined}>
            <bdi dir="auto">{label}</bdi>
            {count !== undefined && count > 0 && <span className="count">{count}</span>}
        </Link>
    );
    return (
        <nav aria-label={t('INDEX')} className="border-b-2 bg-background">
            <div className="mx-auto max-w-content px-gutter">
                <ul className="scroll-x scroll-x-fade -mx-gutter flex gap-2 px-gutter py-2.5 lg:mx-0 lg:px-0">
                    <li className="flex shrink-0">{tile('/', tc('HOME'))}</li>
                    {categories.map(c => (
                        <li key={c.code} className="flex shrink-0">{tile(`/category/${c.description.friendlyUrl}`, c.description.name, countOf(c))}</li>
                    ))}
                    {pages.map((p, i) => (
                        <li key={p.code} className={i === 0 ? 'ms-auto flex shrink-0' : 'flex shrink-0'}>{tile(p.href, p.name)}</li>
                    ))}
                    {extras.map((p, i) => (
                        <li key={p.href} className={i === 0 && pages.length === 0 ? 'ms-auto flex shrink-0' : 'flex shrink-0'}>{tile(p.href, p.label)}</li>
                    ))}
                </ul>
            </div>
        </nav>
    );
}
