import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

export const CardSkeleton = () => (
    <div className="flex flex-col gap-3">
        <Skeleton className="aspect-product w-full rounded-image"/>
        <Skeleton className="h-4 w-3/4"/>
        <Skeleton className="h-4 w-1/3"/>
    </div>
);

export const GridSkeleton = ({count = 8}: { count?: number }) => (
    <div className="grid grid-cols-2 gap-x-4 gap-y-8 lg:grid-cols-3 xl:grid-cols-4" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const Page = PageShell;
