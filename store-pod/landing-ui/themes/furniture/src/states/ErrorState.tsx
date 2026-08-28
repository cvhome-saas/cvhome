'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {isApiError} from '@store-front/types';
import {Button} from '@store-front/ui/button';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {PageShell} from '../components/PageShell';

/** The floor is closed: what went wrong, printed, and both ways out. */
export function ErrorState({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const t = useTranslations('STATES');
    const tc = useTranslations('COMMON');
    const te = useTranslations('ERRORS');
    const body = isApiError(error) && te.has(`CATEGORY.${error.category}`) ? te(`CATEGORY.${error.category}`) : t('ERROR_BODY');
    return (
        <PageShell width="narrow" className="py-section">
            <ErrorBlock className="rule-brass items-start border px-6 py-12 text-start lg:px-10 lg:py-16"
                        title={t('ERROR_TITLE')}
                        action={
                            <>
                                <Button onClick={reset} className="sign text-[0.6875rem]">{tc('RETRY')}</Button>
                                <Button asChild variant="outline" className="sign rule-brass text-[0.6875rem]">
                                    <Link prefetch={false} href="/">{tc('BACK_TO_HOME')}</Link>
                                </Button>
                            </>
                        }>
                <p>{body}</p>
                {error.digest && <p className="figure mt-2 text-xs">{t('ERROR_REFERENCE', {digest: error.digest})}</p>}
            </ErrorBlock>
        </PageShell>
    );
}
