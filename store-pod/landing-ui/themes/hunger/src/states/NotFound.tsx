import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {NotFoundKind} from '@store-front/theme';
import {PageShell} from '../components/PageShell';

const KEYS: Record<NotFoundKind, [string, string]> = {
    product: ['PRODUCT_NOT_FOUND_TITLE', 'PRODUCT_NOT_FOUND_BODY'],
    category: ['CATEGORY_NOT_FOUND_TITLE', 'CATEGORY_NOT_FOUND_BODY'],
    page: ['CONTENT_NOT_FOUND_TITLE', 'CONTENT_NOT_FOUND_BODY'],
    route: ['NOT_FOUND_TITLE', 'NOT_FOUND_BODY'],
};

/** A dish number nobody printed: 404 set at masthead scale, then the way back. */
export async function NotFound({kind}: { kind: NotFoundKind }) {
    const t = await getTranslations('STATES');
    const tc = await getTranslations('COMMON');
    const [title, body] = KEYS[kind];
    return (
        <PageShell width="narrow" className="py-section">
            <p aria-hidden className="press text-6xl leading-none text-primary">404</p>
            <h1 className="press mt-3 border-t-2 border-foreground pt-3 text-3xl leading-none">{t(title)}</h1>
            <p className="mt-2 text-sm text-muted-foreground">{t(body)}</p>
            <Link prefetch={false} href="/" className="fold plate mt-6 h-11 border-primary px-6">{tc('BACK_TO_HOME')}</Link>
        </PageShell>
    );
}
