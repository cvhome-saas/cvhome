import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement, AisleStrip} from '../client';
import {Header} from './Header';
import {Footer} from './Footer';

/*
THESIS: "Cash & Carry." The shop as a working warehouse floor: goods at quantity in molded crates,
prices as printed signage, unit facts on rail tags, state as stickers. It refuses the clean white
produce-app grammar every grocery storefront ships, and the dark gourmet-deli page alike.
OWN-WORLD: concrete floor from the preset background; the merchant PRIMARY as the price-board colour
(hero board, active aisle tile, every action); SECONDARY as the aisle-board wash; one 2px hardware line
on every crate, tile and control; molded-plastic radii; extra-condensed black signage (Fira Sans Extra
Condensed; Almarai leads Arabic) over Manrope body; states print as stickers, never toasts; focus is the
shelf light (ring pinned to primary); one motion clock — everything lands like a stamp.
STORY: the shopper reads the aisle boards, quick-adds by stepper straight from the crates, watches the
basket load in the drawer, and checks out at the counter without leaving the floor.
FIRST VIEWPORT: announcement tape → chunky header (logo · search slot · language/account/basket) →
aisle-board strip (counts whenever the catalog hierarchy provides them) → the price board (store name at signage scale, SHOP NOW) answering the
merchant's slider across a shared seam → the first product rail's crates already in reach.
FORM: cash-and-carry warehouse grammar — candidate 5 of 7 on the grounded list, assigned by the roll;
seed 81c6c91c. Raised by the declined hand: states print themselves (terminal), one motion clock
(algorave), board/image choreography (Brodovitch), total line commitment (menhera), designed attention
(understory), charged quiet between dense passages (ikebana).
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main"
               className="signage sr-only focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50 focus:rounded-control focus:bg-primary focus:px-4 focus:py-2.5 focus:text-sm focus:text-primary-foreground">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <AisleStrip data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
