import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * The plate at the top of a floor that is not a department: checkout, the account, an information page.
 * Same enamel field as the directory board, so every surface in the building is signed the same way.
 */
export function PageHead({title, meta, action, className}: {
    title: ReactNode; meta?: ReactNode; action?: ReactNode; className?: string
}) {
    return (
        <div className={cn('enamel-flat flex flex-wrap items-center justify-between gap-4 px-6 py-5 lg:px-9 lg:py-7', className)}>
            <div className="flex min-w-0 flex-col gap-1.5">
                <h1 className="sign-lg text-xl lg:text-2xl">{title}</h1>
                {meta && <p className="figure text-sm opacity-85">{meta}</p>}
            </div>
            {action}
        </div>
    );
}
