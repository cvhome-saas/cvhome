'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

const item = 'relative flex h-full items-center whitespace-nowrap px-3 font-display text-sm font-semibold uppercase leading-none tracking-wide hover:bg-foreground hover:text-background data-[state=open]:bg-foreground data-[state=open]:text-background focus-visible:outline-2 focus-visible:outline-primary';
// the active zone keeps its tag on: a primary band at the foot of the plate
const activeBand = 'after:absolute after:inset-x-0 after:bottom-0 after:h-1 after:bg-primary';

/**
 * The rail cannot wrap (fixed header height) and must never widen the page, so zones past what a
 * breakpoint fits move under a "More" plate — responsively, with CSS only: a zone's rail plate and
 * its "More" row carry mirrored visibility classes, so exactly one of the pair shows at any width.
 */
const railTier = (index: number): string | null =>
    index < 3 ? '' : index < 5 ? 'hidden xl:block' : index < 6 ? 'hidden 2xl:block' : null;

const moreTier = (index: number): string | null =>
    index < 3 ? null : index < 5 ? 'xl:hidden' : index < 6 ? '2xl:hidden' : '';

/** Quoted zone names in a row, separated by 1px rules; children open as a plate of quoted labels. */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const active = (href: string) => pathname === href || pathname.startsWith(href + '/');
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.inMenu);
    // Main-menu entries that are not pages (blog, help, categories, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    const zones = [
        ...categories.map(category => ({key: category.code, href: `/category/${category.description.friendlyUrl}`,
            label: category.description.name, category})),
        ...pages.map(page => ({key: page.code, href: page.href, label: page.name, category: undefined})),
        ...extras.map(entry => ({key: entry.href, href: entry.href, label: entry.label, category: undefined})),
    ];
    const shown = zones.filter((zone, index) => railTier(index) !== null)
        .map((zone, index) => ({...zone, tier: railTier(index) ?? ''}));
    const overflowZones = zones
        .map((zone, index) => ({...zone, tier: moreTier(index)}))
        .filter((zone): zone is typeof zone & {tier: string} => zone.tier !== null);
    // the More plate itself appears only while it has something to show
    const moreTrigger = overflowZones.length === 0 ? null
        : zones.length > 6 ? '' : zones.length === 6 ? '2xl:hidden' : 'xl:hidden';
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} viewport={false} className={cn('max-w-none justify-start', className)}>
            <NavigationMenuList className="h-full min-w-0 gap-0 divide-x divide-foreground rtl:divide-x-reverse">
                {shown.map(zone => {
                    const children = (zone.category?.children ?? []).filter(c => c.description);
                    const href = zone.href;
                    return (
                        <NavigationMenuItem key={zone.key} className={cn('h-full', zone.tier)}>
                            {children.length > 0 && zone.category ? (
                                <>
                                    <NavigationMenuTrigger className={cn(item, 'rounded-none bg-transparent [&>svg]:ms-1 [&>svg]:size-3', active(href) && activeBand)} aria-current={active(href) ? 'page' : undefined}><span className="q">{zone.label}</span></NavigationMenuTrigger>
                                    <NavigationMenuContent className="plate start-0 mt-0 rounded-none border-foreground p-0 shadow-none">
                                        <ul className="grid w-72 md:w-[32rem] md:grid-cols-2">
                                            <li className="md:col-span-2">
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="block rounded-none border-b border-foreground px-3 py-2 font-display text-sm font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background">
                                                        <span className="q">{t('VIEW_ALL_IN', {name: zone.label})}</span>
                                                    </Link>
                                                </NavigationMenuLink>
                                            </li>
                                            {children.map(child => (
                                                <li key={child.code}>
                                                    <NavigationMenuLink asChild>
                                                        <Link prefetch={false} href={`/category/${child.description.friendlyUrl}`} className="flex items-baseline justify-between gap-3 rounded-none px-3 py-2 font-mono text-xs uppercase tracking-wide hover:bg-foreground hover:text-background">
                                                            <span className="q">{child.description.name}</span>
                                                            {child.productCount > 0 && <span className="tabular-nums opacity-70">{String(child.productCount).padStart(2, '0')}</span>}
                                                        </Link>
                                                    </NavigationMenuLink>
                                                </li>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </>
                            ) : (
                                <NavigationMenuLink asChild className={cn(item, 'rounded-none bg-transparent', active(href) && activeBand)}>
                                    <Link prefetch={false} href={href} aria-current={active(href) ? 'page' : undefined}><span className="q">{zone.label}</span></Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
                {moreTrigger !== null && (
                    <NavigationMenuItem className={cn('h-full', moreTrigger)}>
                        <NavigationMenuTrigger className={cn(item, 'rounded-none bg-transparent [&>svg]:ms-1 [&>svg]:size-3')}><span className="q">{t('MORE')}</span></NavigationMenuTrigger>
                        {/* end-anchored: the last plate's drawer grows inward, never past the page edge */}
                        <NavigationMenuContent className="plate end-0 start-auto mt-0 rounded-none border-foreground p-0 shadow-none">
                            <ul className="w-72">
                                {overflowZones.map(zone => (
                                    <li key={zone.key} className={zone.tier}>
                                        <NavigationMenuLink asChild>
                                            <Link prefetch={false} href={zone.href} className="flex items-baseline rounded-none px-3 py-2 font-mono text-xs uppercase tracking-wide hover:bg-foreground hover:text-background">
                                                <span className="q">{zone.label}</span>
                                            </Link>
                                        </NavigationMenuLink>
                                    </li>
                                ))}
                            </ul>
                        </NavigationMenuContent>
                    </NavigationMenuItem>
                )}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
