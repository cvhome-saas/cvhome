import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * A section opener, the way an issue starts a new feature: the title set in the cover face on an ink rule,
 * its page address on the rule beside it, the section's own action ranged at the end.
 */
export function SectionHeading({title, subtitle, action, pagemark, className, as: Tag = 'h2'}: {
    title: ReactNode; subtitle?: ReactNode; action?: ReactNode; pagemark?: ReactNode; className?: string; as?: 'h1' | 'h2' | 'h3'
}) {
    return (
        <div className={cn('mb-5 flex flex-col gap-2', className)}>
            <div className="hair flex flex-wrap items-end justify-between gap-x-4 gap-y-2 border-b-2 pb-2">
                <div className="flex flex-wrap items-baseline gap-3">
                    {pagemark && <span className="pagemark self-center">{pagemark}</span>}
                    <Tag className="display text-2xl sm:text-3xl">{title}</Tag>
                </div>
                {action}
            </div>
            {subtitle && <p className="text-sm text-muted-foreground">{subtitle}</p>}
        </div>
    );
}
