import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav, MobileNav, HeaderActions, SearchBox} from '../client';

/**
 * The masthead band at the head of the sheet: the merchant's mark set in the printed voice, the standing
 * nav beside it, and the ordering controls at the end. It sits on ink so the sheet below reads as paper.
 */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    return (
        <header className={cn('z-40 border-b-2 border-foreground bg-background', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-header max-w-content items-center gap-2 px-gutter lg:h-header-lg lg:gap-4">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" className="crop flex shrink-0 items-center" aria-label={store.name}>
                    {data.branding.logo ? (
                        <Image src={data.branding.logo.url} alt={data.branding.logo.alt || store.name} width={140} height={36}
                               className="h-8 w-auto object-contain lg:h-9" priority/>
                    ) : (
                        <span className="press text-2xl leading-none lg:text-3xl">{store.name}</span>
                    )}
                </Link>
                <Nav ctx={ctx} data={data} className="hidden min-w-0 flex-1 lg:flex"/>
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
