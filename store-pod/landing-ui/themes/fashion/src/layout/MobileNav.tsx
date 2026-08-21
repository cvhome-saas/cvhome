'use client'
import {useState} from 'react';
import {useTranslations} from 'next-intl';
import {MenuIcon} from 'lucide-react';
import {Link, usePathname} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {Accordion, AccordionContent, AccordionItem, AccordionTrigger} from '@store-front/ui/accordion';
import {Drawer, DrawerContent, DrawerDescription, DrawerHeader, DrawerTitle, DrawerTrigger} from '@store-front/ui/drawer';
import {SearchBox} from '../sections/SearchBox';

/** Mobile navigation: a stretch of wall slides in from the reading-start side; every link is a pasted strip. Closes on navigation. */
export function MobileNav({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const tc = useTranslations('COMMON');
    const pathname = usePathname();
    const [openPath, setOpenPath] = useState<string | null>(null);
    const open = openPath === pathname;
    const setOpen = (o: boolean) => setOpenPath(o ? pathname : null);
    const categories = data.categories.filter(c => c.description);
    const pages = data.pages.filter(p => p.linkToMenu && p.description);
    const link = 'strip strip-hover h-10 w-full justify-start text-base';

    return (
        <Drawer open={open} onOpenChange={setOpen}>
            <DrawerTrigger asChild>
                <button type="button" className="strip strip-hover size-9 justify-center px-0 [--tilt:0deg] lg:hidden" aria-label={t('OPEN_MENU')}><MenuIcon className="size-5"/></button>
            </DrawerTrigger>
            <DrawerContent side="start" className="w-[88vw] max-w-sm overflow-y-auto border-0 bg-(--wall) p-0 [background-image:var(--grain)]">
                <DrawerHeader className="pb-2">
                    <DrawerTitle className="strip h-9 w-fit text-base [--tilt:-1deg]">{tc('MENU')}</DrawerTitle>
                    <DrawerDescription className="sr-only">{t('MAIN_NAVIGATION')}</DrawerDescription>
                </DrawerHeader>
                <nav aria-label={t('MAIN_NAVIGATION')} className="wall-calm wall flex flex-col gap-2 px-4 pb-6">
                    {ctx.layout.search !== 'hidden' && <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="mb-2 [--tilt:0deg]"/>}
                    <Link prefetch={false} href="/" className={link}>{tc('HOME')}</Link>
                    <Accordion type="multiple" className="flex flex-col gap-2">
                        {categories.map(category => {
                            const children = (category.children ?? []).filter(c => c.description);
                            const href = `/category/${category.description.friendlyUrl}`;
                            if (children.length === 0) {
                                return <Link key={category.code} prefetch={false} href={href} className={link}>{category.description.name}</Link>;
                            }
                            return (
                                <AccordionItem key={category.code} value={category.code} className="border-b-0">
                                    <div className="flex items-stretch gap-px">
                                        <Link prefetch={false} href={href} className={`${link} flex-1`}>{category.description.name}</Link>
                                        <AccordionTrigger className="strip strip-hover h-10 w-10 justify-center px-0 py-0 hover:no-underline [&>svg]:size-4" aria-label={category.description.name}/>
                                    </div>
                                    <AccordionContent className="flex flex-col gap-1.5 pb-1 ps-4 pt-2">
                                        {children.map(child => (
                                            <Link key={child.code} prefetch={false} href={`/category/${child.description.friendlyUrl}`} className={`${link} h-9 text-sm`}>
                                                {child.description.name}
                                            </Link>
                                        ))}
                                    </AccordionContent>
                                </AccordionItem>
                            );
                        })}
                    </Accordion>
                    {pages.map(page => (
                        <Link key={page.code} prefetch={false} href={`/content/${page.description.friendlyUrl}`} className={link}>{page.description.name}</Link>
                    ))}
                </nav>
            </DrawerContent>
        </Drawer>
    );
}
