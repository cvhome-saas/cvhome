import {Skeleton} from '@store-front/ui/skeleton';
import {PageShell} from '../../components/PageShell';

/** A blank sheet waiting for its print: paper with a pale picture block and two text lines. */
export const CardSkeleton = () => (
    <div className="sheet flex flex-col gap-2 p-0">
        <Skeleton className="aspect-product w-full rounded-none"/>
        <div className="flex flex-col gap-2 px-3 pb-3 pt-1">
            <Skeleton className="h-4 w-3/4 rounded-none"/>
            <Skeleton className="h-4 w-1/3 rounded-none"/>
        </div>
    </div>
);

export const GridSkeleton = ({count = 8}: { count?: number }) => (
    <div className="wall grid grid-cols-2 gap-x-3 gap-y-5 sm:grid-cols-3 sm:gap-x-4 sm:gap-y-6 lg:grid-cols-4 xl:grid-cols-6" aria-busy>
        {Array.from({length: count}).map((_, i) => <CardSkeleton key={i}/>)}
    </div>
);

export const StripSkeleton = ({className = 'w-40'}: { className?: string }) => <Skeleton className={`h-10 rounded-none ${className}`}/>;

export const Page = PageShell;
