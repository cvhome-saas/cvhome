import {Link} from '@store-front/i18n/navigation';
import type {SectionRenderProps} from '@store-front/theme';
import {linkHref} from '../support';
import {EmptyOrHint} from './shared';

function Promo({section, preview}: SectionRenderProps) {
    if (!section.text.message) {
        return <EmptyOrHint preview={preview} label="Promo — write a message"/>;
    }
    const href = linkHref(section.props.link);
    const card = section.variant === 'card';
    return (
        <div className={card
            ? 'flex flex-col items-center gap-3 rounded-lg bg-muted p-8 text-center'
            : 'flex flex-wrap items-center justify-center gap-3 px-4 py-3 text-center'}>
            <p className={card ? 'text-lg font-medium' : 'text-sm font-medium'}>
                <bdi dir="auto">{section.text.message}</bdi>
            </p>
            {section.text.cta && (
                <Link href={href} className="text-sm font-medium underline underline-offset-4">
                    {section.text.cta}
                </Link>
            )}
        </div>
    );
}

export const promoFallback = {strip: Promo, card: Promo};
