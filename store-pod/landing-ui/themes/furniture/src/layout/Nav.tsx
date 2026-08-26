'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {
    NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger,
} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

/**
 * The masthead carries departments and nothing else — lettered like the signs hung over the aisles, with
 * sub-departments dropping as a ruled floor list. The merchant's information pages live on the utility
 * rail above, because a long "Contact <store name>" page title next to the search field is what pushes a
 * department name off the end of the row.
 */
const ITEM = 'sign whitespace-nowrap rounded-control px-2 py-2 text-[0.625rem] text-foreground transition-colors duration-(--motion-fast) hover:bg-secondary focus-visible:bg-secondary';

export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const categories = data.categories.filter(c => c.description);
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} className={cn('max-w-none justify-start', className)} viewport={false}>
            <NavigationMenuList className="flex-nowrap gap-0">
                {categories.map(category => {
                    const children = (category.children ?? []).filter(c => c.description);
                    const href = `/category/${category.description.friendlyUrl}`;
                    return (
                        <NavigationMenuItem key={category.code}>
                            {children.length > 0 ? (
                                <>
                                    <NavigationMenuTrigger className={cn(ITEM, 'bg-transparent data-[state=open]:bg-secondary')}>
                                        {category.description.name}
                                    </NavigationMenuTrigger>
                                    <NavigationMenuContent>
                                        {/* Two columns only once a floor has enough sub-departments to
                                            need them; a short list should not open a half-empty slab. */}
                                        <ul className={cn('w-64 p-2', children.length > 5 && 'md:grid md:w-[30rem] md:grid-cols-2 md:gap-x-4')}>
                                            <li className={cn('mb-1', children.length > 5 && 'md:col-span-2')}>
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="sign rule-brass block border-b px-2 py-2 text-[0.625rem] hover:bg-secondary">
                                                        {t('VIEW_ALL_IN', {name: category.description.name})}
                                                    </Link>
                                                </NavigationMenuLink>
                                            </li>
                                            {children.map(child => (
                                                <li key={child.code}>
                                                    <NavigationMenuLink asChild>
                                                        <Link prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                              className="flex items-baseline justify-between gap-3 rounded-control px-2 py-1.5 text-sm hover:bg-secondary">
                                                            <span>{child.description.name}</span>
                                                            {child.productCount > 0 && <span className="figure text-xs text-muted-foreground">{child.productCount}</span>}
                                                        </Link>
                                                    </NavigationMenuLink>
                                                </li>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </>
                            ) : (
                                <NavigationMenuLink asChild className={ITEM}>
                                    <Link prefetch={false} href={href}>{category.description.name}</Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
