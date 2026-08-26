import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from './Announcement';
import {Header} from './Header';
import {Footer} from './Footer';

/*
THESIS: The shop is this month's issue of a Japanese girls' fashion magazine — a cover, a contents, ruled
plates of numbered die-cuts with their prices on flags. It refuses the soft millennial-pink boutique of
rounded cards and drop shadows that every pink storefront ships, and its stark monochrome opposite.
OWN-WORLD: paper stock and plum-black ink; the merchant PRIMARY floods whole regions (cover, section
openers, active entries, the basket tab, every primary action) and the ACCENT is the notched price flag
every figure rides; screentone halftone under flooded fields; one 1px ink hairline rules every plate and
cell, no radii except pill controls; the colophon is a solid ink page. Type is Dela Gothic One for
anything that names, prices or flags and M PLUS Rounded 1c for anything that explains (Cairo in Arabic).
STORY: she lands on the cover, reads what this issue is selling, jumps by number to the section she wants,
and adds from the plate; the price is legible before the name, the pen tells her what is nearly gone.
FIRST VIEWPORT: masthead band (wordmark at cover weight · search · language · account · flooded basket tab)
over a ruled contents row of numbered sections. Under it the cover: a flooded screentoned field carrying
the store name at cover scale with the merchant's real cover lines stacked under it and SHOP NOW as a
pill, the first slider image bleeding off the end edge. The cover lines ARE the contents: each is the
numbered address of a section further down the page, so no second contents strip is printed.
FORM: Tokyo Girls Issue (assigned direction, index 3 of the grounded list; chosen by the user over the
pick and the competitive challenger); seed f844e405; code-led.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main"
               className="flood cover-line sr-only px-4 py-2 focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
