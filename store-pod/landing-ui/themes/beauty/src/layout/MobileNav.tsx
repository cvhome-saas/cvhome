'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {MenuIcon} from 'lucide-react';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {SearchBox} from '../sections/SearchBox';

/** Full-width stockroom index on phones: every zone a quoted row with a rule, children as mono sub-rows. */
export function MobileNav({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    // Open state is keyed to the path it was opened on, so a navigation closes it without an effect.
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    const extras = data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/'));
    const row = 'flex items-center border-b border-foreground px-4 py-3 font-display text-lg font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background';

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <button type="button" className="me-3 flex w-10 items-center justify-center border-e border-foreground lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon className="size-5"/></button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-full max-w-none gap-0 overflow-y-auto rounded-none border-foreground bg-background p-0 sm:max-w-sm [&>button]:end-4 [&>button]:top-3.5 [&>button]:rounded-none">
                <DrawerHeader className="border-b border-foreground py-3">
                    <DrawerTitle className="q font-display text-base font-semibold uppercase tracking-wide">{tc('MENU')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col">
                    {ctx.layout.search !== 'hidden' && <div className="border-b border-foreground p-3"><SearchBox storeContext={ctx.storeContext} capabilities={data.search} wide/></div>}
                    <Link prefetch={false} href="/" className={row}><span className="q">{tc('HOME')}</span></Link>
                    <Accordion type="multiple">
                        {categories.map(category => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            if (children.length === 0) return <Link key={category.code} prefetch={false} href={href} className={row}><span className="q">{category.description.name}</span></Link>;
                            return (
                                <AccordionItem key={category.code} value={category.code} className="border-b-0">
                                    <div className="flex items-stretch border-b border-foreground">
                                        <Link prefetch={false} href={href} className="flex flex-1 items-center px-4 py-3 font-display text-lg font-semibold uppercase tracking-wide"><span className="q">{category.description.name}</span></Link>
                                        <AccordionTrigger className="w-12 justify-center border-s border-foreground py-0 hover:no-underline [&>svg]:size-4" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="border-b border-foreground bg-muted p-0">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`} className="q block px-6 py-2.5 font-mono text-sm uppercase tracking-wide hover:bg-foreground hover:text-background">{child.description.name}</Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {pages.map(page => <Link key={page.code} prefetch={false} href={`/content/${page.description.friendlyUrl}`} className={row}><span className="q">{page.description.name}</span></Link>)}
                    {extras.map(node => <Link key={node.href} prefetch={false} href={node.href} className={row}><span className="q">{node.label}</span></Link>)}
                    <div className="hazard h-3 border-b border-foreground" aria-hidden/>
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
