import {getTranslations} from 'next-intl/server';
import {SearchXIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import type {NotFoundKind} from '@store-front/theme';
import {Button} from '@store-front/ui/button';
import {EmptyState as EmptyBlock} from '@store-front/ui/empty-state';
import {PageShell} from '../components/PageShell';

const KEYS: Record<NotFoundKind, [string, string]> = {
    product: ['PRODUCT_NOT_FOUND_TITLE', 'PRODUCT_NOT_FOUND_BODY'],
    category: ['CATEGORY_NOT_FOUND_TITLE', 'CATEGORY_NOT_FOUND_BODY'],
    page: ['CONTENT_NOT_FOUND_TITLE', 'CONTENT_NOT_FOUND_BODY'],
    route: ['NOT_FOUND_TITLE', 'NOT_FOUND_BODY'],
};

export async function NotFound({kind}: { kind: NotFoundKind }) {
    const t = await getTranslations('STATES');
    const tc = await getTranslations('COMMON');
    const [title, body] = KEYS[kind];
    return (
        <PageShell width="narrow" className="py-section">
            <EmptyBlock icon={<SearchXIcon strokeWidth={1.5}/>} title={t(title)}
                        className="border px-6 py-14 [&_[data-slot=empty-state-title]]:font-display [&_[data-slot=empty-state-title]]:text-2xl [&_[data-slot=empty-state-title]]:uppercase"
                        action={<Button asChild><Link prefetch={false} href="/">{tc('BACK_TO_HOME')}</Link></Button>}>
                {t(body)}
            </EmptyBlock>
        </PageShell>
    );
}
