'use client'
import {useTranslations} from 'next-intl';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

const item = 'relative flex h-full items-center whitespace-nowrap px-3 font-display text-sm font-semibold uppercase leading-none tracking-wide hover:bg-foreground hover:text-background data-[state=open]:bg-foreground data-[state=open]:text-background focus-visible:outline-2 focus-visible:outline-primary';
// the active zone keeps its tag on: a primary band at the foot of the plate
const activeBand = 'after:absolute after:inset-x-0 after:bottom-0 after:h-1 after:bg-primary';

/** Quoted zone names in a row, separated by 1px rules; children open as a plate of quoted labels. */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const pathname = usePathname();
    const active = (href: string) => pathname === href || pathname.startsWith(href + '/');
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} viewport={false} className={cn('max-w-none justify-start', className)}>
            <NavigationMenuList className="h-full gap-0 divide-x divide-foreground rtl:divide-x-reverse">
                {categories.map(category => {
                    const children = (category.children ?? []).filter(c => c.description);
                    const href = `/category/${category.description.friendlyUrl}`;
                    return (
                        <NavigationMenuItem key={category.code} className="h-full">
                            {children.length > 0 ? (
                                <>
                                    <NavigationMenuTrigger className={cn(item, 'rounded-none bg-transparent [&>svg]:ms-1 [&>svg]:size-3', active(href) && activeBand)} aria-current={active(href) ? 'page' : undefined}><span className="q">{category.description.name}</span></NavigationMenuTrigger>
                                    <NavigationMenuContent className="plate start-0 mt-0 rounded-none border-foreground p-0 shadow-none">
                                        <ul className="grid w-72 md:w-[32rem] md:grid-cols-2">
                                            <li className="md:col-span-2">
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="block rounded-none border-b border-foreground px-3 py-2 font-display text-sm font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background">
                                                        <span className="q">{t('VIEW_ALL_IN', {name: category.description.name})}</span>
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
                                    <Link prefetch={false} href={href} aria-current={active(href) ? 'page' : undefined}><span className="q">{category.description.name}</span></Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
                {pages.map(page => (
                    <NavigationMenuItem key={page.code} className="h-full">
                        <NavigationMenuLink asChild className={cn(item, 'rounded-none bg-transparent')}>
                            <Link prefetch={false} href={`/content/${page.description.friendlyUrl}`}><span className="q">{page.description.name}</span></Link>
                        </NavigationMenuLink>
                    </NavigationMenuItem>
                ))}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
