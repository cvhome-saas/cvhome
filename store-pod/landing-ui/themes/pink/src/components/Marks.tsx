/**
 * The reader's pen. Four marks, one stroke language (2.2px round caps, drawn on with `.marker-stroke`),
 * used only to point at a fact the shop actually knows — a discount, a last unit, a sold-out die-cut.
 * Never decoration: a mark with nothing to say is not drawn.
 */
const STROKE = 'marker-stroke';

/** A hand ring around a figure. */
export function RingMark({className}: { className?: string }) {
    return (
        <svg viewBox="0 0 64 30" aria-hidden className={className} preserveAspectRatio="none">
            <path className={STROKE} style={{['--len' as string]: 200}}
                  d="M50 5.5C41 1.5 22 1 11 6.5 2.5 10.8 1.5 19 9 23.5c9 5.4 30 6 41 1 8.4-3.8 9.8-11.4 3.2-16"/>
        </svg>
    );
}

/** A curved arrow pointing at what matters. */
export function ArrowMark({className}: { className?: string }) {
    return (
        <svg viewBox="0 0 34 20" aria-hidden className={className}>
            <path className={STROKE} style={{['--len' as string]: 60}} d="M2 4c9 12 20 13 29 12"/>
            <path className={STROKE} style={{['--len' as string]: 30}} d="M24 11.5l7 4.5-6.5 3"/>
        </svg>
    );
}

/** A marker swipe under a line. */
export function UnderMark({className}: { className?: string }) {
    return (
        <svg viewBox="0 0 120 10" aria-hidden className={className} preserveAspectRatio="none">
            <path className={STROKE} style={{['--len' as string]: 130}} d="M2 6.5C28 2.5 70 2 118 5.5"/>
        </svg>
    );
}

/** The four-point sparkle a girls' magazine prints beside the thing it likes. */
export function StarMark({className}: { className?: string }) {
    return (
        <svg viewBox="0 0 24 24" aria-hidden className={className}>
            <path fill="currentColor" d="M12 0c1.1 6.6 5.4 10.9 12 12-6.6 1.1-10.9 5.4-12 12-1.1-6.6-5.4-10.9-12-12C6.6 10.9 10.9 6.6 12 0Z"/>
        </svg>
    );
}
