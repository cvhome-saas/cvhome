import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

export function SectionHeading({title, subtitle, action, className, as: Tag = 'h2'}: {
    title: ReactNode; subtitle?: ReactNode; action?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('mb-6 flex flex-wrap items-end justify-between gap-4', className)}>
            <div>
                <Tag className="text-2xl font-semibold tracking-tight">{title}</Tag>
                {subtitle && <p className="mt-1 text-sm text-muted-foreground">{subtitle}</p>}
            </div>
            {action}
        </div>
    );
}
