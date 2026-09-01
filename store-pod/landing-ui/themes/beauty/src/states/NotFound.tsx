import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {NotFoundKind} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {TagButton} from '../client';

const KEYS: Record<NotFoundKind, [string, string]> = {
    product: ['PRODUCT_NOT_FOUND_TITLE', 'PRODUCT_NOT_FOUND_BODY'],
    category: ['CATEGORY_NOT_FOUND_TITLE', 'CATEGORY_NOT_FOUND_BODY'],
    page: ['CONTENT_NOT_FOUND_TITLE', 'CONTENT_NOT_FOUND_BODY'],
    route: ['NOT_FOUND_TITLE', 'NOT_FOUND_BODY'],
};

/** An empty bin: "404" struck across, the quoted title, a tag back to the floor. */
export async function NotFound({kind}: { kind: NotFoundKind }) {
    const t = await getTranslations('STATES');
    const tc = await getTranslations('COMMON');
    const [title, body] = KEYS[kind];
    return (
        <PageShell width="narrow" className="py-section">
            <section className="plate relative overflow-hidden p-8 text-center">
                <p aria-hidden className="struck font-display text-[7rem] font-bold leading-none tracking-tight text-foreground">404</p>
                <h1 className="q mt-2 font-display text-3xl font-bold uppercase tracking-tight">{t(title)}</h1>
                <p className="mx-auto mt-3 max-w-prose font-mono text-sm text-muted-foreground">{t(body)}</p>
                <TagButton asChild className="mt-6"><Link prefetch={false} href="/">{tc('BACK_TO_HOME')}</Link></TagButton>
            </section>
        </PageShell>
    );
}
