import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

export const CardSkeleton = () => (
    <div className="flex flex-col gap-3">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <Skeleton className="h-4 w-3/4 rounded-none"/>
        <Skeleton className="h-3 w-1/2 rounded-none"/>
        <Skeleton className="h-4 w-1/3 rounded-none"/>
    </div>
);

export const GridSkeleton = ({count = 8}: { count?: number }) => (
    <div className="grid grid-cols-2 ps-px pt-px lg:grid-cols-3 xl:grid-cols-4" aria-busy>
        {Array.from({length: count}).map((_, i) => <div key={i} className="-ms-px -mt-px border border-foreground bg-background p-3"><CardSkeleton/></div>)}
    </div>
);

export const Page = PageShell;
