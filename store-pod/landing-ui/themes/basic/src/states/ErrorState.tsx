'use client'
import {useTranslations} from 'next-intl';
import {AlertTriangleIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import {isApiError} from '@store-front/types';
import {Button} from '@store-front/ui/button';
import {ErrorState as ErrorBlock} from '@store-front/ui/error-state';
import {PageShell} from '../components/PageShell';

export function ErrorState({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const t = useTranslations('STATES');
    const tc = useTranslations('COMMON');
    const te = useTranslations('ERRORS');
    const body = isApiError(error) && te.has(`CATEGORY.${error.category}`) ? te(`CATEGORY.${error.category}`) : t('ERROR_BODY');
    return (
        <PageShell width="narrow" className="py-section">
            <ErrorBlock icon={<AlertTriangleIcon strokeWidth={1.5}/>} title={t('ERROR_TITLE')}
                        className="border px-6 py-14 [&_[data-slot=error-state-title]]:font-display [&_[data-slot=error-state-title]]:text-2xl [&_[data-slot=error-state-title]]:uppercase"
                        action={<><Button onClick={reset}>{tc('RETRY')}</Button><Button asChild variant="outline"><Link prefetch={false} href="/">{tc('BACK_TO_HOME')}</Link></Button></>}>
                <p>{body}</p>
                {error.digest && <p className="mt-1 text-xs tabular-nums">{t('ERROR_REFERENCE', {digest: error.digest})}</p>}
            </ErrorBlock>
        </PageShell>
    );
}
