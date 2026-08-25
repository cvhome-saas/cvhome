import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** The running head: the title in the catalogue's condensed voice at the start, the count and an action at the end, on one rule. */
export function SectionHeading({id, title, meta, subtitle, action, className, as: Tag = 'h2'}: {
    id?: string; title: ReactNode; meta?: ReactNode; subtitle?: ReactNode; action?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('running-head mb-5', className)}>
            <div className="flex min-w-0 flex-col gap-1">
                <div className="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                    <Tag id={id} className="running-head-title"><bdi dir="auto">{title}</bdi></Tag>
                    {meta !== undefined && meta !== null && <span className="text-sm tabular-nums text-muted-foreground">{meta}</span>}
                </div>
                {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
            </div>
            {action && <div className="shrink-0">{action}</div>}
        </div>
    );
}
