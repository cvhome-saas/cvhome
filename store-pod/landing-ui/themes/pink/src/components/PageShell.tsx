import type {ReactNode} from 'react';
import {cn} from '@store-front/ui/lib/utils';

/** The trim of the page. Width follows the theme's layout config; the gutter follows `--gutter`. */
export function PageShell({children, className, width = 'content'}: { children: ReactNode; className?: string; width?: 'narrow' | 'content' | 'wide' | 'fluid' }) {
    return (
        <div className={cn('mx-auto w-full px-gutter', width === 'narrow' && 'max-w-narrow', width === 'content' && 'max-w-content', width === 'wide' && 'max-w-wide', className)}>
            {children}
        </div>
    );
}
