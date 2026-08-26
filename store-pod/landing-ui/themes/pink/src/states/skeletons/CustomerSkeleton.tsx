import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CustomerSkeleton = () => (
    <Page className="flex flex-col gap-6 py-8 lg:py-10">
        <Skeleton className="h-11 w-56 rounded-none"/>
        <Skeleton className="h-10 w-full rounded-none"/>
        <Skeleton className="h-48 w-full rounded-none"/>
    </Page>
);
