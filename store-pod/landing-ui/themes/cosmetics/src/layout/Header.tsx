import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav, MobileNav, HeaderActions, SearchBox} from '../client';

/** Logo · primary nav (desktop) · search · actions. Mobile: menu button · logo · actions. */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    return (
        <header className={cn('z-40 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/80', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-header max-w-content items-center gap-3 px-gutter lg:h-header-lg">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" className="flex shrink-0 items-center gap-2" aria-label={store.name}>
                    {data.branding.logo ? (
                        <Image src={data.branding.logo.url} alt={data.branding.logo.alt || store.name} width={120} height={32} className="h-8 w-auto object-contain" priority/>
                    ) : (
                        <span className="text-lg font-semibold tracking-tight">{store.name}</span>
                    )}
                </Link>
                <Nav ctx={ctx} data={data} className="hidden flex-1 lg:flex"/>
                <div className="ms-auto flex items-center gap-1">
                    {ctx.layout.search === 'header' && (
                        <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden md:block"/>
                    )}
                    <HeaderActions ctx={ctx}/>
                </div>
            </div>
        </header>
    );
}
