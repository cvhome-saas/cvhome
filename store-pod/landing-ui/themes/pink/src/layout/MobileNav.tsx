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

/** The contents page as a drawer: numbered sections, ruled, opening from the reading-start side. */
export function MobileNav({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const th = useTranslations('PAGE.HOME');
    const pathname = usePathname();
    // Open state is keyed to the path it was opened on, so a navigation closes it without an effect.
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.inMenu);
    // Main-menu entries that are not pages (blog, help, categories, plain paths): the merchant's menu, resolved server-side.
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    const entry = 'cover-line flex items-center gap-3 px-4 py-3 hover:bg-secondary';

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <Button variant="ghost" size="icon" className="-ms-2 lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon/></Button>
            </DrawerTrigger>
            <DrawerContent side="start" className="hair w-[88vw] max-w-sm overflow-y-auto rounded-none border-e-2 p-0">
                <DrawerHeader className="flood hair border-b-2 p-4">
                    <DrawerTitle className="display text-2xl">{th('CONTENTS')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col">
                    {ctx.layout.search !== 'hidden' && (
                        <div className="hair border-b p-4">
                            <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="w-full"/>
                        </div>
                    )}
                    <Link prefetch={false} href="/" className={`${entry} hair border-b`}>
                        <span aria-hidden className="dim figure w-6">00</span>{tc('HOME')}
                    </Link>
                    <Accordion type="multiple">
                        {categories.map((category, i) => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            const number = String(i + 1).padStart(2, '0');
                            if (children.length === 0) {
                                return (
                                    <Link key={category.code} prefetch={false} href={href} className={`${entry} hair border-b`}>
                                        <span aria-hidden className="dim figure w-6">{number}</span>{category.description.name}
                                    </Link>
                                );
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code} className="hair border-b">
                                    <div className="flex items-center">
                                        <Link prefetch={false} href={href} className={`${entry} flex-1`}>
                                            <span aria-hidden className="dim figure w-6">{number}</span>{category.description.name}
                                        </Link>
                                        <AccordionTrigger className="w-12 justify-center py-3 hover:no-underline" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="wash pb-0">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`}
                                                  className="flex items-center justify-between gap-2 px-4 py-2.5 ps-13 text-sm font-medium">
                                                <span>{child.description.name}</span>
                                                {child.productCount > 0 && <span className="dim figure text-xs">{child.productCount}</span>}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {pages.map(page => (
                        <Link key={page.code} prefetch={false} href={page.href} className={`${entry} hair border-b`}>{page.name}</Link>
                    ))}
                    {extras.map(page => (
                        <Link key={page.href} prefetch={false} href={page.href} className={`${entry} hair border-b`}>{page.label}</Link>
                    ))}
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
