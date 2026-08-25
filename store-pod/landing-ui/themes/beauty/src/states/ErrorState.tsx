'use client'
import {useTranslations} from 'next-intl';
import {Link} from '@store-front/i18n/navigation';
import {isApiError} from '@store-front/types';
import {PageShell} from '../components/PageShell';
import {TagButton} from '../components/TagButton';

/** A fault plate: hazard band, quoted title, the cause in mono, a retry tag. */
export function ErrorState({error, reset}: { error: Error & { digest?: string }; reset: () => void }) {
    const t = useTranslations('STATES');
    const tc = useTranslations('COMMON');
    const te = useTranslations('ERRORS');
    const body = isApiError(error) && te.has(`CATEGORY.${error.category}`) ? te(`CATEGORY.${error.category}`) : t('ERROR_BODY');
    return (
        <PageShell width="narrow" className="py-section">
            <section role="alert" className="plate relative p-8 text-center">
                <div className="hazard absolute inset-x-0 top-0 h-3" aria-hidden/>
                <h1 className="q mt-4 font-display text-4xl font-bold uppercase leading-none tracking-tight">{t('ERROR_TITLE')}</h1>
                <p className="mx-auto mt-4 max-w-prose font-mono text-sm">{body}</p>
                {error.digest && <p className="mt-2 font-mono text-[0.7rem] uppercase tracking-wide text-muted-foreground">{t('ERROR_REFERENCE', {digest: error.digest})}</p>}
                <div className="mt-6 flex flex-wrap justify-center gap-2">
                    <TagButton onClick={reset}>{tc('RETRY')}</TagButton>
                    <Link prefetch={false} href="/" className="plate inline-flex h-10 items-center px-4 font-display text-sm font-semibold uppercase tracking-wide hover:bg-foreground hover:text-background"><span className="q">{tc('BACK_TO_HOME')}</span></Link>
                </div>
            </section>
        </PageShell>
    );
}
