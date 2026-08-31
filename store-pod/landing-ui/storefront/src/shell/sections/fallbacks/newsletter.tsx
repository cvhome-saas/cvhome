'use client'
import {useState} from 'react';
import type {SectionRenderProps} from '@store-front/theme';

/**
 * A capture form with no list behind it yet: submitting thanks the shopper locally. Wiring it to a real
 * audience list is the newsletter feature's job, not the layout's — the section stays honest either way.
 */
function Newsletter({section}: SectionRenderProps) {
    const [done, setDone] = useState(false);
    const boxed = section.variant === 'boxed';
    const heading = section.text.heading ?? 'Stay in the loop';
    return (
        <div className={boxed ? 'mx-auto max-w-xl rounded-lg border p-8 text-center' : 'mx-auto max-w-xl text-center'}>
            <h2 className="text-lg font-semibold"><bdi dir="auto">{heading}</bdi></h2>
            {section.text.body && (
                <p className="mt-1 text-sm text-muted-foreground"><bdi dir="auto">{section.text.body}</bdi></p>
            )}
            {done ? (
                <p className="mt-4 text-sm font-medium">✓</p>
            ) : (
                <form className="mt-4 flex gap-2" onSubmit={e => { e.preventDefault(); setDone(true); }}>
                    <input type="email" required placeholder="email@example.com"
                           className="h-10 min-w-0 flex-1 rounded-md border bg-background px-3 text-sm"/>
                    <button type="submit" className="h-10 rounded-md bg-primary px-4 text-sm font-medium text-primary-foreground">
                        {section.text.cta ?? '→'}
                    </button>
                </form>
            )}
        </div>
    );
}

export const newsletterFallback = {inline: Newsletter, boxed: Newsletter};
