import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav, MobileNav, HeaderActions, SearchBox} from '../client';

/** The awning rail: logo plate · quoted nav row · search plate · action plates · cart tag. One ink rule below. */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    return (
        // overflow-x-clip: whatever the rail holds, the header must never widen the page
        <header className={cn('z-40 overflow-x-clip border-b border-foreground bg-background', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-header max-w-wide items-stretch px-gutter lg:h-header-lg">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" aria-label={store.name} className="flex shrink-0 items-center gap-2 border-e border-foreground pe-4 ps-1 lg:pe-5">
                    {data.branding.logo
                        ? <Image src={data.branding.logo.url} alt={data.branding.logo.alt || store.name} width={120} height={32} className="h-7 w-auto object-contain lg:h-8" priority/>
                        : <span className="q font-display text-xl font-bold uppercase leading-none tracking-tight">{store.name}</span>}
                </Link>
                <Nav ctx={ctx} data={data} className="hidden min-w-0 flex-1 lg:flex"/>
                <div className="ms-auto flex items-stretch">
                    {ctx.layout.search === 'header' && <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden items-center border-s border-foreground px-3 md:flex"/>}
                    <HeaderActions ctx={ctx}/>
                </div>
            </div>
        </header>
    );
}
