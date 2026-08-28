import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const OrderSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6"><Skeleton className="h-3 w-56"/><Skeleton className="h-10 w-64"/><Skeleton className="h-64 w-full"/></Page>
);
