'use client'
import * as React from 'react';
import {Slot} from '@radix-ui/react-slot';
import {cn} from '@store-front/ui/lib/utils';

/**
 * The zip-tie tag as a control: merchant-primary plate, punched hole at the reading end. Swings once when
 * `swing` flips (the only authored motion in this theme). `asChild` lets it wrap a Link.
 */
export function TagButton({className, asChild, swing, size = 'md', ...props}: React.ComponentProps<'button'> & { asChild?: boolean; swing?: boolean; size?: 'sm' | 'md' | 'lg' }) {
    const Comp = asChild ? Slot : 'button';
    return (
        <Comp
            className={cn(
                'tag inline-flex items-center justify-center gap-2 rounded-control font-display font-semibold uppercase leading-none tracking-wide transition-colors duration-(--motion-fast) ease-standard',
                'hover:bg-primary-hover focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary disabled:cursor-not-allowed disabled:opacity-60',
                size === 'sm' && 'h-8 ps-2.5 text-xs', size === 'md' && 'h-10 ps-3.5 text-sm', size === 'lg' && 'h-12 ps-5 text-base',
                swing && 'tag-swing', className)}
            {...props}
        />
    );
}
