import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from './Announcement';
import {Header} from './Header';
import {IndexStrip} from './IndexStrip';
import {Footer} from './Footer';

/*
THESIS: The store is one continuous ruled catalogue — every product an entry with photo, name, catalogue
number and a big condensed price in the same fixed slots, entries sharing hairline rules instead of
floating as cards, the categories the book's thumb-index; it refuses the white page of rounded, shadowed
cards under a full-bleed slider that every default theme ships.
OWN-WORLD: the preset's paper and ink; 1px rules everywhere, no shadows but on floating surfaces; the
merchant PRIMARY as flat fields only — the cover title block, the active index tab, the one action per
view; Sofia Sans for what explains, Sofia Sans Extra Condensed (Cairo in Arabic) for what names and for
every price; state as a printed stamp in a fixed slot (SALE, SOLD OUT, ONLY N LEFT, ADDED); the price
flash on add as the only motion.
STORY: the shopper reads the index, lands on the cover, scans entries by price, taps an entry; the price
cell floods green and says ADDED.
FIRST VIEWPORT: header (logo · search · language / account / cart) → index strip of category tabs, each
with its product count where the catalogue tree supplies one → the cover: the slider as a ruled stage, 21:9
desktop / 4:3 mobile, height-capped so it never swallows the viewport, numbered page stubs at its bottom-end,
a primary title block overlapping its bottom-start (stacked under it on phones) with the store name at up to
6rem (~6× body), the store's own facts (city · since {year} — the home payload carries no catalogue totals)
and SHOP NOW → the first running head and its ruled entries peeking below.
FORM: The Catalogue Page (assigned direction, index 4 of the re-rolled grounded list; chosen by the user
over the pick and a competitive challenger); seed 28b750f2, re-roll 1; code-led.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main"
               className="plate sr-only px-3 py-2 text-sm font-semibold focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50 focus:rounded-control">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <IndexStrip data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
