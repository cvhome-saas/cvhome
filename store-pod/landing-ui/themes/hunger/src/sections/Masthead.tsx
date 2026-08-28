import {getTranslations} from 'next-intl/server';
import type {Banner, Store} from '@store-front/types';
import {Hero} from './Hero';

/**
 * The head of the sheet — deliberately short, because the menu has to be reachable without scrolling.
 * The store name at printing scale, the facts a takeaway menu actually prints (where it is, what number
 * to call), and the merchant's picture in a ruled frame beside it. With no slider image the type block
 * takes the full measure and the masthead simply gets shorter — the menu starts higher up the page.
 */
export async function Masthead({store, slides}: { store: Store; slides: Banner[] }) {
    const t = await getTranslations('COMPONENTS.FOOTER');
    const place = [store.address?.city, store.address?.country].filter(Boolean).join(' · ');

    return (
        <section className="grid items-start gap-4 py-4 sm:gap-5 lg:grid-cols-[minmax(0,1fr)_20rem] lg:gap-10 lg:py-8" aria-label={store.name}>
            <div className="flex min-w-0 flex-col items-start gap-3">
                <h1 className="press w-full text-4xl leading-none [overflow-wrap:anywhere] sm:text-5xl lg:text-6xl">{store.name}</h1>
                {place && <p className="press text-sm tracking-wide text-muted-foreground">{place}</p>}
                {store.phone && (
                    <a href={`tel:${store.phone}`}
                       className="press mt-1 inline-flex max-w-full flex-wrap items-baseline gap-x-2 border border-foreground px-3 py-1.5 text-xl transition-colors duration-(--motion-fast) hover:bg-primary hover:border-primary hover:text-primary-foreground sm:text-2xl lg:text-3xl">
                        <span className="text-xs tracking-wide opacity-70">{t('CONTACT')}</span>
                        <span dir="ltr">{store.phone}</span>
                    </a>
                )}
            </div>
            {slides.length > 0 && <div className="min-w-0"><Hero slides={slides}/></div>}
        </section>
    );
}
