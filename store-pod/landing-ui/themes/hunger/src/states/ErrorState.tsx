'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {isApiError} from '@store-front/types';
import {PageShell} from '../components/PageShell';

/** A misprint on the sheet: what went wrong, and the two ways back, printed as plates. */
export function ErrorState({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const t = useTranslations('STATES');
    const tc = useTranslations('COMMON');
    const te = useTranslations('ERRORS');
    const body = isApiError(error) && te.has(`CATEGORY.${error.category}`) ? te(`CATEGORY.${error.category}`) : t('ERROR_BODY');
    return (
        <PageShell width="narrow" className="py-section">
            <div className="border-2 border-foreground" role="alert">
                <h1 className="press plate px-4 py-3 text-3xl leading-none">{t('ERROR_TITLE')}</h1>
                <div className="flex flex-col gap-4 p-4">
                    <p className="text-sm text-muted-foreground">{body}</p>
                    {error.digest && <p className="press text-xs tracking-wide text-muted-foreground">{t('ERROR_REFERENCE', {digest: error.digest})}</p>}
                    <div className="flex flex-col gap-2 sm:flex-row">
                        <button type="button" onClick={reset} className="fold plate h-11 flex-1 justify-center border-primary">{tc('RETRY')}</button>
                        <Link prefetch={false} href="/" className="fold h-11 flex-1 justify-center">{tc('BACK_TO_HOME')}</Link>
                    </div>
                </div>
            </div>
        </PageShell>
    );
}
