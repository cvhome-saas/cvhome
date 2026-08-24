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

/** The full index as a ruled list (opens from the reading-start side). Closes on navigation. */
export function MobileNav({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    // Open state is keyed to the path it was opened on, so a navigation closes it without an effect.
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    const categories = data.categories.filter(c => c.description && c.visible !== false);
    const pages = data.pages.filter(p => p.inMenu);
    // Main-menu entries that are not pages (blog, help, categories, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    const row = 'flex min-h-12 flex-1 items-center justify-between gap-3 px-4 py-3 text-base font-medium hover:bg-accent';
    const count = (n: number) => n > 0 && <span className="text-xs tabular-nums text-muted-foreground">{n}</span>;

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <Button variant="ghost" size="icon" className="lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon/></Button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto p-0">
                <DrawerHeader className="border-b">
                    <DrawerTitle className="display text-2xl">{tc('MENU')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col">
                    {ctx.layout.search !== 'hidden' && (
                        <div className="border-b p-4"><SearchBox storeContext={ctx.storeContext} capabilities={data.search}/></div>
                    )}
                    <Link prefetch={false} href="/" className={`${row} border-b`}>{tc('HOME')}</Link>
                    <Accordion type="multiple">
                        {categories.map(category => {
                            const children = (category.children ?? []).filter(c => c.description && c.visible !== false);
                            const href = `/category/${category.description.friendlyUrl}`;
                            if (children.length === 0) {
                                return (
                                    <Link key={category.code} prefetch={false} href={href} className={`${row} border-b`}>
                                        <bdi dir="auto">{category.description.name}</bdi>{count(category.productCount)}
                                    </Link>
                                );
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code}>
                                    <div className="flex items-stretch">
                                        <Link prefetch={false} href={href} className={row}><bdi dir="auto">{category.description.name}</bdi>{count(category.productCount)}</Link>
                                        <AccordionTrigger className="w-12 shrink-0 items-center justify-center border-s py-0 hover:bg-accent hover:no-underline" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="border-t bg-muted/40 pb-0">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                  className="flex min-h-11 items-center justify-between gap-3 border-b px-4 py-2.5 ps-8 text-sm last:border-b-0 hover:bg-accent">
                                                <bdi dir="auto">{child.description.name}</bdi>{count(child.productCount)}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {pages.map(page => (
                        <Link key={page.code} prefetch={false} href={page.href} className={`${row} border-b text-muted-foreground`}>
                            <bdi dir="auto">{page.name}</bdi>
                        </Link>
                    ))}
                    {extras.map(page => (
                        <Link key={page.href} prefetch={false} href={page.href} className={`${row} border-b text-muted-foreground`}>
                            <bdi dir="auto">{page.label}</bdi>
                        </Link>
                    ))}
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
