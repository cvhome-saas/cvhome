import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CustomerSkeleton = () => (
    <Page className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
        <Skeleton className="h-24 w-full rounded-none"/>
        <Skeleton className="h-11 w-full rounded-none"/>
        <Skeleton className="h-56 w-full rounded-none"/>
    </Page>
);
