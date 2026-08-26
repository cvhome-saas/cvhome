import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

/** Skeletons are cut to the same square windows and figure slots the real page uses. */
export const CardSkeleton = () => (
    <div className="flex flex-col gap-3.5">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <Skeleton className="h-4 w-3/4 rounded-none"/>
        <Skeleton className="h-9 w-full rounded-none"/>
    </div>
);

export const GridSkeleton = ({count = 8}: { count?: number }) => (
    <div className="grid grid-cols-2 gap-x-5 gap-y-10 lg:grid-cols-3 lg:gap-x-8 xl:grid-cols-4" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const Page = PageShell;
