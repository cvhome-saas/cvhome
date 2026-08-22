import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const OrderSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-4 w-56"/>
        <div className="running-head"><Skeleton className="h-9 w-64"/><Skeleton className="h-6 w-24"/></div>
        <Skeleton className="h-64 w-full rounded-none"/>
    </Page>
);
