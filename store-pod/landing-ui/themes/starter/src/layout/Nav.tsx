'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {
    NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger,
    navigationMenuTriggerStyle,
} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

/** Desktop primary navigation: categories (children as a panel) + CMS pages flagged for the menu. */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    // Main-menu entries that are not pages (blog, help, categories, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} className={cn('justify-start', className)} viewport={false}>
            <NavigationMenuList className="flex-wrap">
                {categories.map(category => {
                    const children = (category.children ?? []).filter(c => c.description);
                    const href = `/category/${category.description.friendlyUrl}`;
                    return (
                        <NavigationMenuItem key={category.code}>
                            {children.length > 0 ? (
                                <>
                                    <NavigationMenuTrigger>{category.description.name}</NavigationMenuTrigger>
                                    <NavigationMenuContent>
                                        <ul className="grid w-64 gap-1 p-2 md:w-[28rem] md:grid-cols-2">
                                            <li className="md:col-span-2">
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="block rounded-control px-3 py-2 text-sm font-semibold hover:bg-muted">
                                                        {t('VIEW_ALL_IN', {name: category.description.name})}
                                                    </Link>
                                                </NavigationMenuLink>
                                            </li>
                                            {children.map(child => (
                                                <li key={child.code}>
                                                    <NavigationMenuLink asChild>
                                                        <Link prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                              className="block rounded-control px-3 py-2 text-sm hover:bg-muted">
                                                            {child.description.name}
                                                            {child.productCount > 0 && <span className="ms-1 text-xs text-muted-foreground">({child.productCount})</span>}
                                                        </Link>
                                                    </NavigationMenuLink>
                                                </li>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </>
                            ) : (
                                <NavigationMenuLink asChild className={navigationMenuTriggerStyle()}>
                                    <Link prefetch={false} href={href}>{category.description.name}</Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
                {pages.map(page => (
                    <NavigationMenuItem key={page.code}>
                        <NavigationMenuLink asChild className={navigationMenuTriggerStyle()}>
                            <Link prefetch={false} href={`/content/${page.description.friendlyUrl}`}>{page.description.name}</Link>
                        </NavigationMenuLink>
                    </NavigationMenuItem>
                ))}
                {extras.map(page => (
                    <NavigationMenuItem key={page.href}>
                        <NavigationMenuLink asChild className={navigationMenuTriggerStyle()}>
                            <Link prefetch={false} href={page.href}>{page.label}</Link>
                        </NavigationMenuLink>
                    </NavigationMenuItem>
                ))}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
