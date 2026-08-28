import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CustomerSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-12 w-48"/>
        <Skeleton className="h-11 w-full rounded-none"/>
        <Skeleton className="h-48 w-full rounded-none"/>
    </Page>
);
