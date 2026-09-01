import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from '../client';
import {Header} from './Header';
import {Footer} from './Footer';

/**
 * The direction contract. Emitted into the markup below as well as kept here, so it survives the
 * production build and can be audited with `grep 276164e3` over `.next/`.
 */
const CONTRACT = `
THESIS: "The Home Floor Directory." A whole-home retailer is not a catalogue, it is a building with
floors. The shop is rendered as its own directory: departments carry floor numbers and product counts on
one enamel board, every product group is a numbered plate, and the shopper always knows which floor they
are standing on. It refuses the airy-white Scandi arrangement every furniture site ships — whitespace
here is public architecture, not absence — and it refuses the dark spotlit showroom just as hard.
OWN-WORLD: flat bone-plaster ground, warm-graphite ink, the merchant PRIMARY as
whole enamel fields (the directory board, the department plate, the one action per view) with a lit inner
edge and a mounted shadow. Brass hairlines between directory rows and plate keys; the aggregate lives
only in the terrazzo landings between departments, never as a page-wide field; photographs cut square in
hairline windows. Archivo letters the signs expanded and
tracked, Golos Text sets everything read, Tajawal leads Arabic. Every figure — counts, prices, quantity,
floor numbers — sits in one tabular slot. State prints as a word plus a figure, never as a tint.
STORY: a visitor lands facing the building's directory, reads which floors exist — and how much is on each
wherever the catalogue reports a count — walks into one, and finds the same numbering, the same figure
slots and the same enamel action waiting on every surface down to the delivery docket.
FIRST VIEWPORT: thin utility rail (language · account · basket ticket) over the masthead. Below it the
screen splits: on the start side a solid enamel DIRECTORY board carrying the store name at up to 4.25rem,
then one row per department — big tabular floor number, name in tracked expanded caps, the product count
where the catalogue reports one, and a wayfinding mark closing every row at the measure, brass hairlines
between — with the single primary action on the board itself; on the end
side the merchant's slider runs full-bleed in a hairline window with a ruled caption plate in its lower
corner. Mobile stacks the board first, the window under it. No slider image: the board fills the screen
and the window becomes a drawn department plate, still finished.
FORM: The Home Floor Directory — candidate 3 of my grounded list, dealt by the roll; seed 276164e3,
code-led (no image generation on this machine). Raised by the hand it beat: state as a printed word plus
figure and never a tint (exposure record); quantities as the interface, one tabular slot per figure that
rolls in place when it changes (nixie counter); real dimensions and materials pinned to the photograph on
leader lines (tensegrity column); numbered plates and a registered room/on-white gallery pair (curved
crease); an authored department plate where the merchant supplied no picture (rocketship plate).
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict,
DESIGN.md, and every shipping raster carrying its provenance.
`;

/** Page frame: contract → skip link → announcement → header → main → footer. Server component. */
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <div hidden aria-hidden dangerouslySetInnerHTML={{__html: `<!--${CONTRACT}-->`}}/>
            <a href="#main"
               className="sign sr-only text-xs focus:not-sr-only focus:fixed focus:start-3 focus:top-3 focus:z-50 focus:rounded-control focus:bg-primary focus:px-4 focus:py-2.5 focus:text-primary-foreground focus:shadow-md">
                {t('SKIP_TO_CONTENT')}
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
