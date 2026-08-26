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

/** The sheet unfolded: every section with its count, then the small print. Closes on navigation. */
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
                <Button variant="ghost" size="icon" className="lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon/></Button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto border-e-2 border-foreground p-0">
                <DrawerHeader className="border-b-2 border-foreground">
                    <DrawerTitle className="press text-2xl">{tc('MENU')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col px-4 py-3">
                    {ctx.layout.search !== 'hidden' && <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="mb-3"/>}
                    <Link prefetch={false} href="/" className="press border-b border-border py-2.5 text-lg">{tc('HOME')}</Link>
                    <Accordion type="multiple">
                        {categories.map(category => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            const label = (
                                <>
                                    {category.description.name}
                                    {category.productCount > 0 && (
                                        <span className="ms-2 font-sans text-xs font-medium tabular-nums text-muted-foreground">{category.productCount}</span>
                                    )}
                                </>
                            );
                            if (children.length === 0) {
                                return (
                                    <Link key={category.code} prefetch={false} href={href} className="press flex items-baseline border-b border-border py-2.5 text-lg">
                                        {label}
                                    </Link>
                                );
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code} className="border-b border-border">
                                    <div className="flex items-center">
                                        <Link prefetch={false} href={href} className="press flex flex-1 items-baseline py-2.5 text-lg">{label}</Link>
                                        <AccordionTrigger className="w-10 justify-center py-2 hover:no-underline" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="ps-4">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                  className="block border-t border-border py-2 text-sm">
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    <div className="mt-5 flex flex-col gap-1 border-t-2 border-foreground pt-3">
                        {pages.map(page => (
                            <Link key={page.code} prefetch={false} href={page.href} className="py-1.5 text-sm text-muted-foreground">{page.name}</Link>
                        ))}
                        {extras.map(page => (
                            <Link key={page.href} prefetch={false} href={page.href} className="py-1.5 text-sm text-muted-foreground">{page.label}</Link>
                        ))}
                    </div>
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
