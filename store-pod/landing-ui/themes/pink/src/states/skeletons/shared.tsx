import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

/** Skeletons print as blank cells on the same ruled plate, so nothing shifts when the goods arrive. */
export const CardSkeleton = () => (
    <div className="flex h-full flex-col">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <div className="hair flex flex-col gap-2 border-t p-3">
            <Skeleton className="h-4 w-3/4 rounded-none"/>
            <Skeleton className="h-6 w-1/2 rounded-none"/>
            <Skeleton className="h-8 w-full rounded-control"/>
        </div>
    </div>
);

export const GridSkeleton = ({count = 10}: { count?: number }) => (
    <div className="plate grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const Page = PageShell;
