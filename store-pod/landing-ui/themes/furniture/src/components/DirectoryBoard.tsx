import type {ReactNode} from 'react';
import {ChevronRightIcon} from 'lucide-react';
import {Link} from '@store-front/i18n/navigation';
import {cn} from '@store-front/ui/lib/utils';

// A directory row always ends at the measure: the count when the catalogue reports one, and the way-
// finding mark that points to the floor either way. An enamel row that stops half-way is a broken sign.
const COLS = {
    with: 'grid-cols-[3rem_1fr_auto_1.25rem] lg:grid-cols-[4rem_1fr_auto_1.5rem]',
    without: 'grid-cols-[3rem_1fr_1.25rem] lg:grid-cols-[4rem_1fr_1.5rem]',
} as const;

export interface Department {
    code: string;
    name: string;
    href: string;
    count: number;
}

/**
 * The escalator-hall directory board: one enamel field, one row per department — floor number, name, item
 * count — separated by hairlines. It is the shop's table of contents and its first viewport; the footer
 * repeats it small as the building's colophon.
 *
 * Floor numbers are the department's position in the merchant's own category order, so the number a
 * shopper reads on the board is the number they see again on the department's own plate.
 */
export function DirectoryBoard({title, facts, departments, action, headings, className}: {
    title: ReactNode;
    facts?: string[];
    departments: Department[];
    action?: ReactNode;
    /** Column captions for the table head: floor · department · items. */
    headings: { floor: string; department: string; items: string };
    className?: string;
}) {
    // The catalogue does not always report a product count per department; when none of them has one,
    // the ITEMS column is dropped rather than printed as a row of dashes.
    const counted = departments.some(d => d.count > 0);
    return (
        <div className={cn('enamel-flat flex flex-col p-6 sm:p-8 lg:p-10', className)}>
            <h1 className="sign-lg text-2xl sm:text-3xl lg:text-4xl">{title}</h1>
            {facts && facts.length > 0 && (
                <p className="mt-3 text-sm opacity-80">{facts.join(' · ')}</p>
            )}
            {action && <div className="mt-6">{action}</div>}

            {departments.length > 0 && (
                <div className="mt-8 lg:mt-10">
                    <div className={cn('rule-enamel grid items-end gap-x-4 border-b pb-2 text-[0.6875rem] opacity-70', COLS[counted ? 'with' : 'without'])}>
                        <span className="sign">{headings.floor}</span>
                        <span className="sign">{headings.department}</span>
                        {counted && <span className="sign text-end">{headings.items}</span>}
                        <span/>
                    </div>
                    <ul>
                        {departments.map((d, i) => (
                            <li key={d.code} className="rule-enamel border-b last:border-b-0">
                                <Link prefetch={false} href={d.href}
                                      className={cn('group grid items-baseline gap-x-4 py-3 transition-colors duration-(--motion-fast) hover:bg-[color-mix(in_srgb,var(--primary-foreground)_12%,transparent)] focus-visible:bg-[color-mix(in_srgb,var(--primary-foreground)_12%,transparent)] lg:py-4', COLS[counted ? 'with' : 'without'])}>
                                    <span className="floor-no text-2xl opacity-90 lg:text-3xl">{String(i + 1).padStart(2, '0')}</span>
                                    <span className="sign text-sm group-hover:underline lg:text-base">{d.name}</span>
                                    {counted && <span className="figure text-sm opacity-80 lg:text-base">{d.count > 0 ? d.count : '—'}</span>}
                                    <ChevronRightIcon aria-hidden
                                                      className="size-4 self-center opacity-55 transition-transform duration-(--motion-fast) group-hover:translate-x-0.5 rtl:rotate-180 rtl:group-hover:-translate-x-0.5"/>
                                </Link>
                            </li>
                        ))}
                    </ul>
                </div>
            )}
        </div>
    );
}
