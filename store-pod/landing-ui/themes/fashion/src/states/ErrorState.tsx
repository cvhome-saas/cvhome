'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {isApiError} from '@store-front/types';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {PageShell} from '../components/PageShell';

/** Something tore: a sheet with the title stamped in the sale colour, the cause printed under it, retry on day-glo. */
export function ErrorState({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const t = useTranslations('STATES');
    const tc = useTranslations('COMMON');
    const te = useTranslations('ERRORS');
    const body = isApiError(error) && te.has(`CATEGORY.${error.category}`) ? te(`CATEGORY.${error.category}`) : t('ERROR_BODY');
    return (
        <PageShell width="narrow" className="py-section">
            <ErrorBlock className="sheet sheen peel stamp-title stamp-title-xl stamp-title-sale px-6 [--tilt:-0.6deg]" title={t('ERROR_TITLE')}
                        action={<><button type="button" className="glo h-11 px-5 text-sm" onClick={reset}>{tc('RETRY')}</button><Link prefetch={false} href="/" className="strip strip-hover h-11 px-5 [--tilt:0deg]">{tc('BACK_TO_HOME')}</Link></>}>
                <p>{body}</p>
                {error.digest && <p className="mt-1 text-xs tabular-nums">{t('ERROR_REFERENCE', {digest: error.digest})}</p>}
            </ErrorBlock>
        </PageShell>
    );
}
