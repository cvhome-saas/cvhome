import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * A plate head. Every group of goods in this shop is a numbered plate, the way a furnishing catalogue
 * numbers its room sets: an enamel number square, the plate's name, the count of what is on it, and a
 * brass hairline running to the end of the measure.
 */
export function SectionHeading({plate, title, meta, action, id, className, as: Tag = 'h2'}: {
    /** Zero-padded plate number, e.g. "01". Omit on heads that are not a plate (search results, related). */
    plate?: string;
    title: ReactNode;
    meta?: ReactNode;
    action?: ReactNode;
    id?: string;
    className?: string;
    as?: 'h1' | 'h2' | 'h3';
}) {
    return (
        <div className={cn('mb-7 flex flex-wrap items-center gap-x-5 gap-y-3 lg:mb-9', className)}>
            {plate && (
                <span aria-hidden className="enamel-flat floor-no grid size-11 place-items-center text-lg lg:size-14 lg:text-2xl">
                    {plate}
                </span>
            )}
            <Tag id={id} className="sign-lg text-xl lg:text-3xl">{title}</Tag>
            {meta && <span className="figure text-sm text-muted-foreground lg:text-base">{meta}</span>}
            <span aria-hidden className="rule-brass hidden h-px min-w-8 flex-1 border-t sm:block"/>
            {action}
        </div>
    );
}
