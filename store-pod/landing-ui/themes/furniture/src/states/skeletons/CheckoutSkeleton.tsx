import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CheckoutSkeleton = () => (
    <Page className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
        <Skeleton className="h-24 w-full rounded-none"/>
        <div className="grid gap-10 lg:grid-cols-3 lg:gap-14">
            <div className="flex flex-col gap-5 lg:col-span-2">
                {Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-11 w-full rounded-none"/>)}
            </div>
            <Skeleton className="h-72 w-full rounded-none"/>
        </div>
    </Page>
);
