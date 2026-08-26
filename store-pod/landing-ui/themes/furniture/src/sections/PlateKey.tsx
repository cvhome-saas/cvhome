import type {ReactNode} from 'react';

export interface KeyRow {
    label: string;
    value: ReactNode;
}

/**
 * The plate key. A furnishing catalogue numbers the things in its plate and lists them down the side,
 * each number joined to its entry by a leader; this is that list, and it is where a shopper reads the
 * piece's real facts instead of opening an accordion to look for them.
 *
 * The rows are the merchant's own facts — on a furniture store the dimensions, materials, finish and
 * care. It deliberately holds nothing the buy box already prints two hundred pixels above (maker,
 * catalogue number, floor, availability): a leader running to a fact the reader has just read adds
 * length, not information. Nothing here is invented, and with no attributes there is no key at all.
 */
export function PlateKey({rows}: { rows: KeyRow[] }) {
    if (rows.length === 0) return null;
    return (
        <dl>
            {rows.map((row, i) => (
                <div key={`${row.label}-${i}`} className="rule-brass flex items-baseline gap-3 border-b py-3 last:border-b-0">
                    <span aria-hidden className="figure w-7 shrink-0 text-xs text-muted-foreground">{String(i + 1).padStart(2, '0')}</span>
                    <dt className="shrink-0 text-sm text-muted-foreground">{row.label}</dt>
                    <span aria-hidden className="rule-brass h-px min-w-4 flex-1 self-center border-b border-dotted"/>
                    <dd className="figure text-end text-sm">{row.value}</dd>
                </div>
            ))}
        </dl>
    );
}
