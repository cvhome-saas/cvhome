import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const OrderSkeleton = () => (
    <Page className="flex flex-col gap-8 py-6 lg:py-10">
        <Skeleton className="h-3.5 w-56 rounded-none"/>
        <Skeleton className="h-24 w-full rounded-none"/>
        <Skeleton className="h-72 w-full rounded-none"/>
    </Page>
);
