import Image from 'next/image';
import {Link} from '@store-front/i18n/navigation';
import type {LayoutData, PageContext} from '@store-front/theme';
import {cn} from '@store-front/ui/lib/utils';
import {MobileNav} from './MobileNav';
import {HeaderActions} from './HeaderActions';
import {SearchBox} from '../sections/SearchBox';

/**
 * The counter: menu (mobile) · logo / wordmark · the search slot (md+) · language / account / basket.
 * Category navigation is not here — it is the aisle-board strip under the counter (AisleStrip).
 */
export function Header({ctx, data}: { ctx: PageContext; data: LayoutData }) {
    const {store} = data;
    return (
        <header className={cn('z-40 border-b-2 bg-background', ctx.layout.header.sticky && 'sticky top-0')}>
            <div className="mx-auto flex h-header max-w-content items-center gap-2 px-gutter lg:h-header-lg lg:gap-5">
                <MobileNav ctx={ctx} data={data}/>
                <Link prefetch={false} href="/" className="flex min-w-0 shrink-0 items-center" aria-label={store.name}>
                    {store.logo?.path ? (
                        <Image src={store.logo.path} alt={store.logo.name || store.name} width={160} height={40} className="h-9 w-auto max-w-[11rem] object-contain lg:h-10" priority/>
                    ) : (
                        <span className="signage truncate text-2xl lg:text-3xl"><bdi dir="auto">{store.name}</bdi></span>
                    )}
                </Link>
                {ctx.layout.search === 'header' && (
                    <SearchBox storeContext={ctx.storeContext} capabilities={data.search} className="hidden md:block md:w-full md:max-w-sm lg:max-w-md"/>
                )}
                <div className="ms-auto flex shrink-0 items-center gap-1">
                    <HeaderActions ctx={ctx}/>
                </div>
            </div>
        </header>
    );
}
