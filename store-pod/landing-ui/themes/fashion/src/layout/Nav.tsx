'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {
    NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger,
} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

const TILTS = ['[--tilt:0.7deg]', '[--tilt:-0.6deg]', '[--tilt:0.9deg]', '[--tilt:-0.8deg]', '[--tilt:0.5deg]'];
const STRIP = 'strip strip-hover h-9 rounded-none bg-background px-3 text-sm hover:bg-foreground hover:text-background focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring data-[state=open]:bg-foreground data-[state=open]:text-background';

/** Desktop nav: each category a pasted strip (alternating tilts); children open on a sheet. CMS pages flagged for the menu follow. */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    let i = 0;
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} className={cn('justify-start', className)} viewport={false}>
            <NavigationMenuList className="flex-wrap gap-1.5">
                {categories.map(category => {
                    const children = (category.children ?? []).filter(c => c.description);
                    const href = `/category/${category.description.friendlyUrl}`;
                    const tilt = TILTS[i++ % TILTS.length];
                    return (
                        <NavigationMenuItem key={category.code}>
                            {children.length > 0 ? (
                                <>
                                    <NavigationMenuTrigger className={cn(STRIP, tilt, '[&>svg]:size-3')}>{category.description.name}</NavigationMenuTrigger>
                                    <NavigationMenuContent className="sheet !rounded-none !border-0 p-2 [--tilt:0deg]">
                                        <ul className="grid w-64 gap-0.5 md:w-[30rem] md:grid-cols-2">
                                            <li className="md:col-span-2">
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="block px-3 py-2 font-display text-base uppercase tracking-wide hover:bg-foreground hover:text-background">
                                                        {t('VIEW_ALL_IN', {name: category.description.name})}
                                                    </Link>
                                                </NavigationMenuLink>
                                            </li>
                                            {children.map(child => (
                                                <li key={child.code}>
                                                    <NavigationMenuLink asChild>
                                                        <Link prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                              className="flex items-baseline justify-between gap-3 px-3 py-2 text-sm hover:bg-foreground hover:text-background">
                                                            <span>{child.description.name}</span>
                                                            {child.productCount > 0 && <span className="text-xs tabular-nums opacity-70">{child.productCount}</span>}
                                                        </Link>
                                                    </NavigationMenuLink>
                                                </li>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </>
                            ) : (
                                <NavigationMenuLink asChild className={cn(STRIP, tilt)}>
                                    <Link prefetch={false} href={href}>{category.description.name}</Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
                {pages.map(page => {
                    const tilt = TILTS[i++ % TILTS.length];
                    return (
                        <NavigationMenuItem key={page.code}>
                            <NavigationMenuLink asChild className={cn(STRIP, tilt)}>
                                <Link prefetch={false} href={`/content/${page.description.friendlyUrl}`}>{page.description.name}</Link>
                            </NavigationMenuLink>
                        </NavigationMenuItem>
                    );
                })}
                {extras.map(node => {
                    const tilt = TILTS[i++ % TILTS.length];
                    return (
                        <NavigationMenuItem key={node.href}>
                            <NavigationMenuLink asChild className={cn(STRIP, tilt)}>
                                <Link prefetch={false} href={node.href}>{node.label}</Link>
                            </NavigationMenuLink>
                        </NavigationMenuItem>
                    );
                })}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
