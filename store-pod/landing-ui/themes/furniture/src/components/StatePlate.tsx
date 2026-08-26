import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/**
 * State is printed, never tinted: a plate carries the word and — when there is one — the figure. Remove
 * every colour from this page and sale, low stock and sold out still read.
 */
export function StatePlate({children, tone = 'ink', className}: {
    children: ReactNode; tone?: 'ink' | 'sale' | 'quiet' | 'enamel'; className?: string
}) {
    const tones = {
        ink: 'text-foreground',
        sale: 'text-sale',
        quiet: 'text-muted-foreground',
        enamel: 'border-transparent bg-primary text-primary-foreground',
    };
    return <span className={cn('state-plate', tones[tone], className)}>{children}</span>;
}
