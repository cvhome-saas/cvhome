import {getTranslations} from 'next-intl/server';
import {Link} from '@store-front/i18n/navigation';
import type {NotFoundKind} from '@store-front/theme';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';
import {PageShell} from '../components/PageShell';

const KEYS: Record<NotFoundKind, [string, string]> = {
    product: ['PRODUCT_NOT_FOUND_TITLE', 'PRODUCT_NOT_FOUND_BODY'],
    category: ['CATEGORY_NOT_FOUND_TITLE', 'CATEGORY_NOT_FOUND_BODY'],
    page: ['CONTENT_NOT_FOUND_TITLE', 'CONTENT_NOT_FOUND_BODY'],
    route: ['NOT_FOUND_TITLE', 'NOT_FOUND_BODY'],
};

/** The poster that was torn down: a sheet with the title as a big stamp. */
export async function NotFound({kind}: { kind: NotFoundKind }) {
    const t = await getTranslations('STATES');
    const tc = await getTranslations('COMMON');
    const [title, body] = KEYS[kind];
    return (
        <PageShell width="narrow" className="py-section">
            <EmptyBlock className="sheet sheen peel stamp-title stamp-title-xl px-6 [--tilt:-0.6deg]" title={t(title)}
                        action={<Link prefetch={false} href="/" className="glo h-11 px-5 text-sm">{tc('BACK_TO_HOME')}</Link>}>
                {t(body)}
            </EmptyBlock>
        </PageShell>
    );
}
