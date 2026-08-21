import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CheckoutSkeleton = () => (
    <Page className="flex flex-col gap-6 py-8">
        <Skeleton className="h-9 w-48"/>
        <div className="grid gap-8 lg:grid-cols-3">
            <div className="flex flex-col gap-4 lg:col-span-2">{Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}</div>
            <Skeleton className="h-64 w-full rounded-none"/>
        </div>
    </Page>
);
