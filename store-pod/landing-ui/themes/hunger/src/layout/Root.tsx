import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement, FoldStrip} from '../client';
import {Header} from './Header';
import {Footer} from './Footer';

/*
THESIS: "The Letterbox Menu." The store is the folded takeaway menu posted through the door: every dish a
printed line — order number, name, dotted leader, price in one tabular column, ADD at the end — with
sections as bands of the second ink plate and symbols where photos will not fit. It refuses the full-bleed
dark dish photo over rounded cards with a coloured add button that every food storefront ships, and it
refuses the cream-and-serif fine-dining page just as hard.
OWN-WORLD: two plates on white menu stock — ink and the merchant PRIMARY, nothing else. Zero radius
everywhere; hairline rules instead of cards; the primary as flat fields only (masthead block, section
band, active fold tab, every ADD, the ordered dish's number box). Alumni Sans prints what names and what
counts (masthead, bands, numbers, prices), Geologica sets what must be read, Alexandria leads Arabic.
State is printed in outline — SALE / SOLD OUT / ONLY N LEFT — never a toast. Attention is a registration
crop mark, not a browser ring.
STORY: a hungry visitor lands already inside the menu, reads down one column of dishes, taps ADD, sees the
second plate come down across the line, and watches its number box stay filled with what they ordered.
FIRST VIEWPORT: header band (logo · search · language / account / basket tally) → the fold strip of
category tabs, the active one lit full width → a short masthead: store name at up to 5.25rem beside the
merchant's slider in a ruled frame, one facts line, the sheet's own contents count → the first section
band and its dish lines already in reach, addable without scrolling.
FORM: The Letterbox Menu — candidate 1 of my grounded list, chosen by the user over the dealt direction;
seed 6e711b79, code-led. Raised by the declined hand: designed attention as crop marks (viewfinder),
full-width active state (grid horizon), charged quiet between dense sections (ikebana), one tabular price
column down the whole sheet (datamatics).
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main"
               className="press sr-only px-3 py-2 text-sm focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50 focus:bg-primary focus:text-primary-foreground">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <FoldStrip data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
