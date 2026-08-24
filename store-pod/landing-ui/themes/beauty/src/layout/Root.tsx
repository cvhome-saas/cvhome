import {getTranslations} from 'next-intl/server';
import type {RootLayoutProps} from '@store-front/theme';
import {Announcement} from './Announcement';
import {Header} from './Header';
import {Footer} from './Footer';

/*
THESIS: "Labels stay on." A beauty + fashion boutique run like a stockroom: every zone and control named
literally in straight quotes, facts printed on ink plates, the merchant's primary colour as the one zip-tie
tag. It refuses the airy blush beauty page and the luxury black-and-gold page alike.
OWN-WORLD: cotton ground and ink from the merchant preset; 1px ink plates with square corners; 45° hazard
stripes as structure and as "struck" state; condensed caps display in straight quotes (Oswald); mono labels
and numerals (JetBrains Mono); the tag plate with a punched hole carries every primary action; state is a
mark (tag on, stripe across), never a tint; no shadows below the overlay.
STORY: the shopper reads what the store is in one quoted line, sees the goods as labelled items with price,
brand and SKU printed on the plate, adds with a tag that swings once, trusts the facts and comes back.
FIRST VIEWPORT: awning rail header (logo plate · quoted nav row · search plate · language/account plates ·
cart tag). Hero: display headline = the store's name in straight quotes with tag CTAs on the start side to the
shelves below (HomeData carries product groups, not categories; the nav already names the categories); the merchant's slider images in a plate frame with a 01/N pager on the end side. Below:
one shelf per product group, heading plate + hazard rule, item plates butted on a 1px grid.
FORM: Industrial Quote Grammar (dealt challenger, chosen by the user over the assigned Attar Stall); seed 85f13d63.
FINISH: unreviewed and undocumented is unfinished; this build ends with the finish review, the verdict, and DESIGN.md
*/
export async function Root({ctx, data, children}: RootLayoutProps) {
    const t = await getTranslations('COMMON');
    return (
        <>
            <a href="#main" className="sr-only focus:not-sr-only focus:fixed focus:start-2 focus:top-2 focus:z-50 focus:bg-primary focus:px-3 focus:py-2 focus:font-mono focus:text-sm focus:uppercase focus:text-primary-foreground">
                <span className="q">{t('SKIP_TO_CONTENT')}</span>
            </a>
            {data.announcement && <Announcement announcement={data.announcement}/>}
            <Header ctx={ctx} data={data}/>
            <main id="main" className="flex-1">{children}</main>
            <Footer ctx={ctx} data={data}/>
        </>
    );
}
