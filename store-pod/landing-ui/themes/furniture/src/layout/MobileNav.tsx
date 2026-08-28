'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {MenuIcon} from 'lucide-react';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {Button} from '@store-front/ui/button';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {SearchBox} from '../sections/SearchBox';

/**
 * The directory, carried in the hand: the same floor numbers, names and counts as the board, opening from
 * the reading-start side. Closes on navigation.
 */
export function MobileNav({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    // Open state is keyed to the path it was opened on, so a navigation closes it without an effect.
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.inMenu);
    // Main-menu entries that are not pages (blog, help, categories, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <Button variant="ghost" size="icon" className="-ms-2 lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon/></Button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-[90vw] max-w-sm overflow-y-auto p-0">
                <DrawerHeader className="enamel-flat rounded-none px-5 py-4">
                    <DrawerTitle className="sign-lg text-lg">{tc('DIRECTORY')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col px-4 py-3">
                    {ctx.layout.search !== 'hidden' && (
                        <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="mb-4"/>
                    )}
                    <Link prefetch={false} href="/" className="sign rule-brass border-b py-3 text-[0.6875rem]">{tc('HOME')}</Link>
                    <Accordion type="multiple">
                        {categories.map((category, i) => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            const floor = <span aria-hidden className="floor-no w-8 shrink-0 text-lg text-muted-foreground">{String(i + 1).padStart(2, '0')}</span>;
                            if (children.length === 0) {
                                return (
                                    <Link key={category.code} prefetch={false} href={href}
                                          className="rule-brass flex items-center gap-3 border-b py-3">
                                        {floor}
                                        <span className="sign flex-1 text-[0.6875rem]">{category.description.name}</span>
                                        {category.productCount > 0 && <span className="figure text-sm text-muted-foreground">{category.productCount}</span>}
                                    </Link>
                                );
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code} className="rule-brass border-b">
                                    <div className="flex items-center gap-3">
                                        {floor}
                                        <Link prefetch={false} href={href} className="sign flex-1 py-3 text-[0.6875rem]">{category.description.name}</Link>
                                        {category.productCount > 0 && <span className="figure text-sm text-muted-foreground">{category.productCount}</span>}
                                        <AccordionTrigger className="w-9 justify-center py-3 hover:no-underline" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="ps-11">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                  className="flex items-baseline justify-between gap-3 py-2 text-sm">
                                                <span>{child.description.name}</span>
                                                {child.productCount > 0 && <span className="figure text-xs text-muted-foreground">{child.productCount}</span>}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {[...pages.map(p => ({key: p.code, href: p.href, label: p.name})), ...extras.map(p => ({key: p.href, href: p.href, label: p.label}))].map(l => (
                        <Link key={l.key} prefetch={false} href={l.href} className="rule-brass border-b py-3 text-sm">{l.label}</Link>
                    ))}
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
