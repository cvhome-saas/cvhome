'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData} from '@store-front/theme';

/**
 * The fold: the menu's section index, printed straight under the masthead band. Every top-level category
 * is a tab; the one you are reading lights across its whole width. Counts are printed whenever the
 * catalogue tree supplies them — a menu tells you how many dishes are in each section.
 */
export function FoldStrip({data}: { data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const categories = data.categories.filter(c => c.description);
    if (categories.length === 0) return null;
    return (
        <nav aria-label={t('INDEX')} className="crease border-b border-border bg-background">
            <div className="scroll-x mx-auto max-w-content px-gutter">
                <ul className="flex w-max items-stretch gap-0">
                    {categories.map(category => {
                        const href = `/category/${category.description.friendlyUrl}`;
                        const active = pathname === href;
                        return (
                            <li key={category.code} className="-ms-px first:ms-0">
                                <Link prefetch={false} href={href} className="fold" aria-current={active ? 'page' : undefined}>
                                    {category.description.name}
                                    {category.productCount > 0 && <span className="count">{category.productCount}</span>}
                                </Link>
                            </li>
                        );
                    })}
                </ul>
            </div>
        </nav>
    );
}
