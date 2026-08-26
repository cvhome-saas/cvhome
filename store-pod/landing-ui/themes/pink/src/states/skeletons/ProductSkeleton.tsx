import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ProductSkeleton = () => (
    <Page className="flex flex-col gap-8 py-6 lg:py-10">
        <Skeleton className="h-4 w-48 rounded-none"/>
        <div className="grid gap-8 lg:grid-cols-2 lg:gap-12">
            <Skeleton className="aspect-square w-full rounded-none"/>
            <div className="flex flex-col gap-4">
                <Skeleton className="h-4 w-28 rounded-none"/>
                <Skeleton className="h-10 w-3/4 rounded-none"/>
                <Skeleton className="h-12 w-40 rounded-none"/>
                <Skeleton className="h-6 w-56 rounded-none"/>
                <Skeleton className="mt-4 h-12 w-full rounded-control"/>
            </div>
        </div>
    </Page>
);
