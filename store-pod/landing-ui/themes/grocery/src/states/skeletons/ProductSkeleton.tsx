import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ProductSkeleton = () => (
    <Page className="flex flex-col gap-10 py-6 lg:py-8">
        <Skeleton className="h-4 w-48"/>
        <div className="grid gap-6 lg:grid-cols-2 lg:gap-10">
            <Skeleton className="aspect-product w-full rounded-card"/>
            <div className="flex flex-col gap-4"><Skeleton className="h-10 w-3/4"/><Skeleton className="h-12 w-40"/><Skeleton className="h-10 w-full"/><Skeleton className="h-12 w-1/2"/></div>
        </div>
    </Page>
);
