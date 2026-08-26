import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ProductSkeleton = () => (
    <Page className="flex flex-col gap-10 py-6 lg:gap-16 lg:py-10">
        <Skeleton className="h-3.5 w-48 rounded-none"/>
        <div className="grid gap-8 lg:grid-cols-[1.05fr_1fr] lg:gap-14">
            <Skeleton className="aspect-product w-full rounded-none"/>
            <div className="flex flex-col gap-5">
                <Skeleton className="h-9 w-3/4 rounded-none"/>
                <Skeleton className="h-11 w-40 rounded-none"/>
                <Skeleton className="h-12 w-full rounded-none"/>
                <Skeleton className="h-12 w-2/3 rounded-none"/>
            </div>
        </div>
    </Page>
);
