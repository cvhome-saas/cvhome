import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ProductSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-4 w-48"/>
        <div className="grid gap-6 lg:grid-cols-2 lg:gap-12">
            <Skeleton className="aspect-product w-full rounded-none"/>
            <div className="flex flex-col gap-4">
                <Skeleton className="h-4 w-24"/>
                <Skeleton className="h-9 w-3/4"/>
                <Skeleton className="h-12 w-40"/>
                <Skeleton className="h-3 w-28"/>
                <div className="mt-4 flex gap-2"><Skeleton className="h-9 w-16"/><Skeleton className="h-9 w-16"/><Skeleton className="h-9 w-16"/></div>
                <div className="mt-2 flex gap-3"><Skeleton className="h-10 w-28"/><Skeleton className="h-12 flex-1"/></div>
            </div>
        </div>
    </Page>
);
