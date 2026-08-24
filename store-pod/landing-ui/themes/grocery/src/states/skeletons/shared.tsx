import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

/** A loading crate: the cell shape holds while the goods arrive. */
export const CardSkeleton = () => (
    <div className="flex flex-col overflow-hidden rounded-card border-2">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <div className="flex flex-col gap-2 border-t-2 p-3">
            <Skeleton className="h-4 w-3/4"/>
            <Skeleton className="h-7 w-1/2"/>
        </div>
    </div>
);

export const GridSkeleton = ({count = 10}: { count?: number }) => (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 sm:gap-4 lg:grid-cols-4 xl:grid-cols-5" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const Page = PageShell;
