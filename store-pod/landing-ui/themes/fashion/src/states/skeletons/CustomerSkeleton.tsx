import {Skeleton} from '@store-front/ui/skeleton';
import {Page, StripSkeleton} from './shared';

export const CustomerSkeleton = () => (
    <Page width="content" className="flex flex-col gap-6 py-6 lg:py-8">
        <StripSkeleton className="h-12 w-48"/>
        <div className="flex gap-2"><Skeleton className="h-10 flex-1 rounded-none"/><Skeleton className="h-10 flex-1 rounded-none"/><Skeleton className="h-10 flex-1 rounded-none"/></div>
        <div className="sheet p-5"><Skeleton className="h-40 w-full rounded-none"/></div>
    </Page>
);
