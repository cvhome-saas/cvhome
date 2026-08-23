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

/** Mobile navigation drawer (opens from the reading-start side). Closes on navigation. */
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
    const linkCls = 'block rounded-control px-3 py-2 text-base font-medium hover:bg-muted';

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <Button variant="ghost" size="icon" className="lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon/></Button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto p-0">
                <DrawerHeader className="border-b">
                    <DrawerTitle>{tc('MENU')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="flex flex-col gap-1 p-2">
                    {ctx.layout.search !== 'hidden' && <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="mb-2"/>}
                    <Link prefetch={false} href="/" className={linkCls}>{tc('HOME')}</Link>
                    <Accordion type="multiple">
                        {categories.map(category => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            if (children.length === 0) {
                                return <Link key={category.code} prefetch={false} href={href} className={linkCls}>{category.description.name}</Link>;
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code} className="border-b-0">
                                    <div className="flex items-center">
                                        <Link prefetch={false} href={href} className={`${linkCls} flex-1`}>{category.description.name}</Link>
                                        <AccordionTrigger className="w-10 justify-center py-2 hover:no-underline" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="ps-4">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`} className={`${linkCls} text-sm font-normal`}>
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {pages.map(page => (
                        <Link key={page.code} prefetch={false} href={`/content/${page.description.friendlyUrl}`} className={linkCls}>{page.description.name}</Link>
                    ))}
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
