import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** A label strip pasted at the start of a stretch of wall: the group title in poster caps, a count stub, an optional action. */
export function SectionHeading({id, title, meta, action, className, as: Tag = 'h2'}: {
    id?: string; title: ReactNode; meta?: ReactNode; action?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('mb-5 flex flex-wrap items-center gap-3', className)}>
            <Tag id={id} className="strip h-10 text-base [--tilt:-0.6deg] sm:h-11 sm:text-lg" dir="auto"><bdi>{title}</bdi></Tag>
            {meta !== undefined && <span className="strip h-8 bg-foreground text-background text-xs tabular-nums [--tilt:0.9deg]" aria-hidden>{meta}</span>}
            {action && <div className="ms-auto">{action}</div>}
        </div>
    );
}
