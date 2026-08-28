import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ProductSkeleton = () => (
    <Page className="flex flex-col gap-8 py-6">
        <Skeleton className="h-3 w-48"/>
        <div className="grid gap-8 lg:grid-cols-2 lg:gap-12">
            <Skeleton className="aspect-product w-full"/>
            <div className="flex flex-col gap-4">
                <Skeleton className="h-6 w-24"/><Skeleton className="h-12 w-3/4"/><Skeleton className="h-10 w-full"/>
                <Skeleton className="h-9 w-full"/><Skeleton className="h-12 w-full"/>
            </div>
        </div>
    </Page>
);
