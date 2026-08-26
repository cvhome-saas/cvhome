'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {
    NavigationMenu, NavigationMenuContent, NavigationMenuItem, NavigationMenuLink, NavigationMenuList, NavigationMenuTrigger,
} from '@store-front/ui/navigation-menu';
import {cn} from '@store-front/ui/lib/utils';

const ENTRY = 'cover-line flex min-h-9 flex-row shrink-0 items-center whitespace-nowrap px-3 py-1.5 transition-colors duration-(--motion-fast) hover:bg-primary hover:text-primary-foreground data-[state=open]:bg-primary data-[state=open]:text-primary-foreground';

/** The contents row: the issue's sections (the merchant's categories), each a tracked cover line
 *  that floods when it is the way in. CMS pages ride the title row above — they are not sections. */
export function Nav({data, className}: { ctx: PageContext; data: LayoutData; className?: string }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const categories = data.categories.filter(c => c.description);
    return (
        <NavigationMenu aria-label={t('MAIN_NAVIGATION')} className={cn('max-w-none justify-start', className)} viewport={false}>
            <NavigationMenuList className="-mx-3 flex-wrap justify-start gap-0">
                {categories.map((category, i) => {
                    const children = (category.children ?? []).filter(c => c.description);
                    const href = `/category/${category.description.friendlyUrl}`;
                    const number = String(i + 1).padStart(2, '0');
                    return (
                        <NavigationMenuItem key={category.code}>
                            {children.length > 0 ? (
                                <>
                                    <NavigationMenuTrigger className={cn(ENTRY, 'rounded-none bg-transparent')}>
                                        <span aria-hidden className="dim me-2">{number}</span>{category.description.name}
                                    </NavigationMenuTrigger>
                                    <NavigationMenuContent className="hair rounded-none border-2 p-0">
                                        <ul className="grid w-64 md:w-[30rem] md:grid-cols-2">
                                            <li className="hair border-b md:col-span-2">
                                                <NavigationMenuLink asChild>
                                                    <Link prefetch={false} href={href} className="cover-line block rounded-none px-3 py-2.5 hover:bg-primary hover:text-primary-foreground focus:bg-primary focus:text-primary-foreground">
                                                        {t('VIEW_ALL_IN', {name: category.description.name})}
                                                    </Link>
                                                </NavigationMenuLink>
                                            </li>
                                            {children.map(child => (
                                                <li key={child.code}>
                                                    <NavigationMenuLink asChild>
                                                        <Link prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                              className="flex flex-row items-center justify-between gap-2 rounded-none px-3 py-2 text-sm font-medium hover:bg-secondary focus:bg-secondary">
                                                            <span>{child.description.name}</span>
                                                            {child.productCount > 0 && <span className="dim figure text-xs">{child.productCount}</span>}
                                                        </Link>
                                                    </NavigationMenuLink>
                                                </li>
                                            ))}
                                        </ul>
                                    </NavigationMenuContent>
                                </>
                            ) : (
                                <NavigationMenuLink asChild className={cn(ENTRY, 'rounded-none')}>
                                    <Link prefetch={false} href={href}>
                                        <span aria-hidden className="dim me-2">{number}</span>{category.description.name}
                                    </Link>
                                </NavigationMenuLink>
                            )}
                        </NavigationMenuItem>
                    );
                })}
            </NavigationMenuList>
        </NavigationMenu>
    );
}
