import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** A hanging aisle board: the section name in the signage voice, facts or actions at the end of the rail. */
export function SectionHeading({title, subtitle, meta, action, className, id, as: Tag = 'h2'}: {
    title: ReactNode; subtitle?: ReactNode; meta?: ReactNode; action?: ReactNode; className?: string; id?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('mb-5 flex flex-wrap items-end justify-between gap-x-4 gap-y-2 border-b-2 pb-3', className)}>
            <div className="flex min-w-0 items-baseline gap-3">
                <Tag id={id} className="signage text-2xl lg:text-3xl"><bdi dir="auto">{title}</bdi></Tag>
                {meta !== undefined && <span className="text-sm font-semibold tabular-nums text-muted-foreground">{meta}</span>}
            </div>
            {subtitle && <p className="w-full text-sm text-muted-foreground">{subtitle}</p>}
            {action}
        </div>
    );
}
