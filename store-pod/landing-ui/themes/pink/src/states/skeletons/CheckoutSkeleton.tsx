import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CheckoutSkeleton = () => (
    <Page className="flex flex-col gap-6 py-8 lg:py-10">
        <Skeleton className="h-11 w-56 rounded-none"/>
        <div className="grid gap-8 lg:grid-cols-3 lg:gap-10">
            <div className="flex flex-col gap-4 lg:col-span-2">{Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-11 w-full rounded-control"/>)}</div>
            <Skeleton className="h-72 w-full rounded-none"/>
        </div>
    </Page>
);
