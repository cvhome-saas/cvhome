import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * A section band: the running head that names a stretch of the menu, printed on the second plate so the
 * eye can find it while scrolling a long sheet. The optional action sits inside the band, at its end.
 */
export function SectionHeading({title, subtitle, action, className, as: Tag = 'h2'}: {
    title: ReactNode; subtitle?: ReactNode; action?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('mb-5', className)}>
            <div className="plate flex flex-wrap items-center justify-between gap-x-4 gap-y-1 px-3 py-1.5">
                <Tag className="press text-xl leading-none sm:text-2xl">{title}</Tag>
                {action}
            </div>
            {subtitle && <p className="mt-2 text-sm text-muted-foreground">{subtitle}</p>}
        </div>
    );
}
