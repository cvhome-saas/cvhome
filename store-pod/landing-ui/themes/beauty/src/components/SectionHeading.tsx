import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** A quoted display label on a plate, a hazard rule running to the edge, an optional action at the end. */
export function SectionHeading({title, meta, action, className, as: Tag = 'h2', id}: {
    title: ReactNode; meta?: ReactNode; action?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'; id?: string
}) {
    return (
        <div className={cn('mb-5 flex items-stretch gap-3', className)}>
            <Tag id={id} className="plate rivet shrink-0 ps-6 pe-3 py-1.5 font-display text-2xl font-semibold uppercase leading-none tracking-tight"><span className="q">{title}</span></Tag>
            <div className="hazard-soft min-w-6 flex-1 self-stretch" aria-hidden/>
            {meta && <span className="plate flex shrink-0 items-center px-2 font-mono text-base leading-none tracking-wide tabular-nums">{meta}</span>}
            {action}
        </div>
    );
}
