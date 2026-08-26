'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';

/**
 * The standing row beside the masthead: the merchant's information pages and menu entries, set as the
 * sheet's small print. Categories are not repeated here — they are the fold strip under this band.
 */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const links = [
        ...data.pages.filter(p => p.inMenu).map(p => ({key: p.code, href: p.href, label: p.name})),
        ...data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
    ];
    if (links.length === 0) return null;
    return (
        <nav aria-label={t('MAIN_NAVIGATION')} className={cn('items-center gap-5', className)}>
            {links.map(l => (
                <Link key={l.key} prefetch={false} href={l.href} aria-current={pathname === l.href ? 'page' : undefined}
                      className={cn('press whitespace-nowrap text-sm tracking-wide text-foreground/75 transition-colors duration-(--motion-fast) hover:text-foreground',
                          pathname === l.href && 'text-foreground underline decoration-primary decoration-2 underline-offset-4')}>
                    {l.label}
                </Link>
            ))}
        </nav>
    );
}
