import {Skeleton} from '@store-front/ui/skeleton';
import {Page, StripSkeleton} from './shared';

export const CheckoutSkeleton = () => (
    <Page width="content" className="flex flex-col gap-6 py-6 lg:py-8">
        <StripSkeleton className="h-7 w-40"/><StripSkeleton className="h-12 w-48"/>
        <div className="grid gap-6 lg:grid-cols-3 lg:gap-8">
            <div className="sheet flex flex-col gap-4 p-7 lg:col-span-2">{Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-10 w-full rounded-none"/>)}</div>
            <div className="sheet p-4"><Skeleton className="h-64 w-full rounded-none"/></div>
        </div>
    </Page>
);
