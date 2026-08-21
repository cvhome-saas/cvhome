import {Skeleton} from '@store-front/ui/skeleton';
import {Page, StripSkeleton} from './shared';

export const OrderSkeleton = () => (
    <Page width="content" className="flex flex-col gap-6 py-6 lg:py-8">
        <StripSkeleton className="h-7 w-64"/><StripSkeleton className="h-11 w-56"/>
        <div className="sheet"><Skeleton className="h-64 w-full rounded-none"/></div>
    </Page>
);
