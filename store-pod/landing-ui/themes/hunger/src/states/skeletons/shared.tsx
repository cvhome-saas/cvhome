import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

/** A dish line before it is printed: the number slot, the name, the price column. */
export const CardSkeleton = () => (
    <div className="flex items-center gap-3 border-b border-border py-3.5">
        <Skeleton className="h-6 w-10"/>
        <div className="flex flex-1 flex-col gap-2"><Skeleton className="h-4 w-2/3"/><Skeleton className="h-3 w-1/2"/></div>
        <Skeleton className="h-6 w-16"/>
    </div>
);

export const GridSkeleton = ({count = 8}: { count?: number }) => (
    <div className="grid border-t border-border" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const Page = PageShell;
