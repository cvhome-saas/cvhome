import {getTranslations} from 'next-intl/server';
import {ChevronRightIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {NotFoundKind} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

const KEYS: Record<NotFoundKind, [string, string]> = {
    product: ['PRODUCT_NOT_FOUND_TITLE', 'PRODUCT_NOT_FOUND_BODY'],
    category: ['CATEGORY_NOT_FOUND_TITLE', 'CATEGORY_NOT_FOUND_BODY'],
    page: ['CONTENT_NOT_FOUND_TITLE', 'CONTENT_NOT_FOUND_BODY'],
    route: ['NOT_FOUND_TITLE', 'NOT_FOUND_BODY'],
};

/**
 * No such floor, said in the board's own grammar: a directory row with the floor column struck through
 * and the missing entry where a department name would be, closed by the same wayfinding mark every real
 * row carries — pointing back at the directory itself rather than at a bare home button.
 *
 * The state contract hands this component only `kind`, with no store context, so it cannot list the
 * departments; it sends the reader to the board on the home page, which is that list.
 */
export async function NotFound({kind}: { kind: NotFoundKind }) {
    const t = await getTranslations('STATES');
    const [title, body] = KEYS[kind];

    return (
        <PageShell width="content" className="py-section">
            <section role="status" className="rule-brass border">
                <div className="rule-brass grid grid-cols-[3rem_1fr] items-baseline gap-x-4 border-b px-6 py-7 lg:grid-cols-[4rem_1fr] lg:px-9 lg:py-9">
                    <span aria-hidden className="floor-no text-3xl text-muted-foreground line-through decoration-2 lg:text-5xl">00</span>
                    <div className="flex flex-col gap-3">
                        <h1 className="sign-lg text-xl lg:text-2xl">{t(title)}</h1>
                        <p className="max-w-[48ch] text-sm text-muted-foreground">{t(body)}</p>
                    </div>
                </div>

                <Link prefetch={false} href="/"
                      className="group grid grid-cols-[3rem_1fr_1.25rem] items-baseline gap-x-4 px-6 py-5 transition-colors duration-(--motion-fast) hover:bg-secondary lg:grid-cols-[4rem_1fr_1.5rem] lg:px-9">
                    <span aria-hidden className="floor-no text-2xl text-muted-foreground lg:text-3xl">01</span>
                    <span className="sign text-sm group-hover:underline">{t('BACK_TO_DIRECTORY')}</span>
                    <ChevronRightIcon aria-hidden className="size-4 self-center text-muted-foreground transition-transform duration-(--motion-fast) group-hover:translate-x-0.5 rtl:rotate-180 rtl:group-hover:-translate-x-0.5"/>
                </Link>
            </section>
        </PageShell>
    );
}
