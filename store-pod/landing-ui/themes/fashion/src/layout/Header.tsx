import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {Nav, MobileNav, HeaderActions, SearchBox, Wordmark} from '../client';

/** The top of the wall: wordmark strip · nav strips · search strip · language/account strips · day-glo cart stub. */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    return (
        <header className={cn('z-40 bg-(--wall)/90 backdrop-blur-sm', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-header max-w-wide items-center gap-2 px-gutter lg:h-header-lg lg:gap-3">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" aria-label={store.name} className="strip h-9 min-w-0 max-w-[44vw] shrink-0 sm:max-w-[60vw] px-2.5 text-lg [--tilt:-1deg] lg:h-10 lg:px-3 lg:text-xl">
                    <Wordmark logo={data.branding.logo} name={store.name}/>
                </Link>
                <Nav ctx={ctx} data={data} className="hidden min-w-0 flex-1 lg:flex"/>
                <div className="ms-auto flex items-center gap-2">
                    {ctx.layout.search === 'header' && <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden md:block"/>}
                    <HeaderActions ctx={ctx}/>
                </div>
            </div>
        </header>
    );
}
