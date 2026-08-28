import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const OrderSkeleton = () => (
    <Page className="flex flex-col gap-6 py-8 lg:py-10">
        <Skeleton className="h-4 w-56 rounded-none"/>
        <Skeleton className="h-11 w-64 rounded-none"/>
        <Skeleton className="h-64 w-full rounded-none"/>
    </Page>
);
