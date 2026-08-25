import {Skeleton} from '@store-front/ui/skeleton';
import {cn} from '@store-front/ui/lib/utils';
import {PageShell} from '../../components/PageShell';

/** One entry's slots while it loads: photo, two name lines, number, price. */
export const CardSkeleton = () => (
    <div className="flex flex-col">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <div className="flex flex-col gap-2 border-t px-3 pb-3 pt-2.5">
            <Skeleton className="h-3.5 w-4/5"/>
            <Skeleton className="h-3.5 w-1/2"/>
            <Skeleton className="h-3 w-1/3"/>
            <div className="mt-2 flex items-end justify-between"><Skeleton className="h-6 w-16"/><Skeleton className="size-9"/></div>
        </div>
    </div>
);

export const GridSkeleton = ({count = 8, className}: { count?: number; className?: string }) => (
    <div className={cn('ruled grid-cols-2 lg:grid-cols-3 xl:grid-cols-4', className)} aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

/** A running head's slots. */
export const HeadSkeleton = () => (
    <div className="running-head mb-5"><Skeleton className="h-8 w-48"/><Skeleton className="h-4 w-10"/></div>
);

export const Page = PageShell;
