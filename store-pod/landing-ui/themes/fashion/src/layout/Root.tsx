import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from './Announcement';
import {Header} from './Header';
import {Footer} from './Footer';

/*
THESIS: The store is a fly-posted wall. Every slider image and every product is a pasted paper sheet, so
the goods ARE the page and nothing is drawn around them; it refuses the black-and-white streetwear
brutalism of a full-bleed lookbook hero over a four-column grid.
OWN-WORLD: a rendered wall (the preset's background mixed toward its foreground, with grain) under paper
sheets in the preset's background colour, offset soft shadows, ≤1.2° deterministic tilts, a peeling corner
on the big posters; the merchant PRIMARY is the day-glo paper stock and owns every primary action and live
state; type is poster caps (Anton, Changa in Arabic) for anything that names and Rubik for anything that
explains; state is a rubber stamp (SALE, SOLD OUT, ONLY N LEFT, ADDED), never a tint; no radii, no borders
except pencil rules.
STORY: the shopper reads the wall in one glance — name sheet, the merchant's posters, the goods pasted
around them with the price printed on every foot — taps a poster, and the ADDED stamp slaps on.
FIRST VIEWPORT: header = wordmark strip · nav strips · search strip · language/account strips · day-glo cart
stub. Below: the hero wall — the store's name sheet (H1, city and year, SHOP NOW day-glo strip) on the
start side, the first slider image as a big peeling poster in the middle, one product poster on the end
side; then the first group's strip label and its posters, six across. No carousel.
FORM: The Wheatpaste Wall (assigned direction, index 3 of the grounded list; chosen by the user over the
pick and three competitive challengers); seed 569a4b15; code-led.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main" className="glo sr-only px-3 py-2 text-sm focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement box={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
