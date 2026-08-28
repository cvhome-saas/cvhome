import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CustomerSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6"><Skeleton className="h-12 w-64"/><Skeleton className="h-10 w-full"/><Skeleton className="h-48 w-full"/></Page>
);
