import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav} from './Nav';
import {MobileNav} from './MobileNav';
import {HeaderActions} from './HeaderActions';
import {SearchBox} from '../sections/SearchBox';

/**
 * The masthead. A heavy two-rule band: the title row carries the merchant's name at cover weight with the
 * imprint links and utilities ranged at the end, and a ruled contents row under it lists the sections of
 * the issue. The contents row grows rather than clipping when a store has many sections.
 */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    // CMS pages and the merchant's non-page menu entries are the imprint, not sections of the issue.
    const imprint = [
        ...data.pages.filter(p => p.inMenu).map(p => ({key: p.code, href: p.href, label: p.name})),
        ...data.menus.main.filter(n => n.kind !== 'PAGE' && n.href.startsWith('/')).map(n => ({key: n.href, href: n.href, label: n.label})),
    ];
    return (
        <header className={cn('hair z-40 border-b-2 bg-background', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-14 max-w-content items-center gap-2 px-gutter lg:h-[4.25rem] lg:gap-5">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" className="flex shrink-0 items-center" aria-label={store.name}>
                    {store.logo?.path ? (
                        <Image src={store.logo.path} alt={store.logo.name || store.name} width={200} height={50}
                               className="h-8 w-auto object-contain lg:h-10" priority/>
                    ) : (
                        <span className="display text-xl lg:text-3xl">{store.name}</span>
                    )}
                </Link>
                {imprint.length > 0 && (
                    <nav className="hidden min-w-0 xl:flex" aria-label={store.name}>
                        <ul className="flex flex-wrap items-center gap-x-5">
                            {imprint.map(p => (
                                <li key={p.key}>
                                    <Link prefetch={false} href={p.href} className="cover-line text-muted-foreground underline-offset-4 hover:text-foreground hover:underline">
                                        {p.label}
                                    </Link>
                                </li>
                            ))}
                        </ul>
                    </nav>
                )}
                <div className="ms-auto flex items-center gap-1 lg:gap-2">
                    {ctx.layout.search === 'header' && (
                        <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden md:block"/>
                    )}
                    <HeaderActions ctx={ctx}/>
                </div>
            </div>
            <div className="hair hidden border-t lg:block">
                <div className="mx-auto flex min-h-9 max-w-content items-center px-gutter">
                    <Nav ctx={ctx} data={data} className="flex-1"/>
                </div>
            </div>
        </header>
    );
}
