import Image from 'next/image';
import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav} from './Nav';
import {MobileNav} from './MobileNav';
import {HeaderActions} from './HeaderActions';
import {SearchBox} from '../sections/SearchBox';

/**
 * The entrance. From lg up it is two rows, the way a shop front is: a thin ink utility rail carrying the
 * building's own facts and the three visitor controls, and beneath it the masthead — mark, departments,
 * search. Below lg the rail folds into the single masthead row.
 */
export async function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const {store} = data;
    const railFacts = [store.address?.city, store.phone].filter(Boolean) as string[];
    // The merchant's information pages and any non-category menu entries. They belong on the rail, not in
    // the masthead: page titles here run long ("Contact <store name>") and would crowd out a department.
    const railLinks = [
        ...data.pages.filter(p => p.inMenu).map(p => ({key: p.code, href: p.href, label: p.name})),
        ...data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
    ];

    return (
        <header className={cn('rule-brass z-40 border-b bg-background', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="hidden bg-foreground text-background lg:block">
                <div className="mx-auto flex h-9 max-w-content items-center gap-6 px-gutter">
                    {railFacts.length > 0 && (
                        <p className="sign shrink-0 truncate text-[0.625rem] opacity-75">{railFacts.join(' · ')}</p>
                    )}
                    {railLinks.length > 0 && (
                        <nav aria-label={t('INFORMATION')} className="min-w-0 overflow-hidden">
                            <ul className="flex flex-nowrap items-center gap-5">
                                {railLinks.map(l => (
                                    <li key={l.key}>
                                        <Link prefetch={false} href={l.href}
                                              className="sign whitespace-nowrap text-[0.625rem] opacity-75 transition-opacity duration-(--motion-fast) hover:opacity-100 hover:underline">
                                            {l.label}
                                        </Link>
                                    </li>
                                ))}
                            </ul>
                        </nav>
                    )}
                    <HeaderActions ctx={ctx} className="ms-auto shrink-0"/>
                </div>
            </div>

            <div className="mx-auto flex h-header max-w-content items-center gap-3 px-gutter lg:h-[calc(var(--header-h-lg)-2.25rem)] lg:gap-8">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" className="flex shrink-0 items-center" aria-label={store.name}>
                    {data.branding.logo ? (
                        <Image src={data.branding.logo.url} alt={data.branding.logo.alt || store.name} width={160} height={40}
                               className="h-8 w-auto max-w-[9rem] object-contain lg:h-10 lg:max-w-[12rem]" priority/>
                    ) : (
                        <span className="sign-lg text-base lg:text-xl">{store.name}</span>
                    )}
                </Link>
                <Nav ctx={ctx} data={data} className="hidden min-w-0 flex-1 lg:flex"/>
                <div className="ms-auto flex items-center gap-1">
                    {ctx.layout.search === 'header' && (
                        <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden w-64 md:block lg:w-80"/>
                    )}
                    <HeaderActions ctx={ctx} className="lg:hidden"/>
                </div>
            </div>
        </header>
    );
}
