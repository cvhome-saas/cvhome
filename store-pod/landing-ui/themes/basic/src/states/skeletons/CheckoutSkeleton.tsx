import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const CheckoutSkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-4 w-32"/>
        <Skeleton className="h-12 w-56"/>
        <div className="grid gap-8 lg:grid-cols-3 lg:gap-12">
            <div className="flex flex-col gap-4 lg:col-span-2">
                <Skeleton className="h-7 w-48"/>
                <div className="grid gap-4 sm:grid-cols-2">{Array.from({length: 4}).map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}</div>
                <div className="grid gap-4 sm:grid-cols-3">{Array.from({length: 3}).map((_, i) => <Skeleton key={i} className="h-10 w-full"/>)}</div>
                <Skeleton className="h-20 w-full"/>
            </div>
            <Skeleton className="h-64 w-full rounded-none"/>
        </div>
    </Page>
);
