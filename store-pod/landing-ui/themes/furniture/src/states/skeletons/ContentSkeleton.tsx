import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ContentSkeleton = () => (
    <Page width="narrow" className="flex flex-col gap-5 py-6 lg:py-10">
        <Skeleton className="h-3.5 w-40 rounded-none"/>
        <Skeleton className="h-24 w-full rounded-none"/>
        {Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-4 w-full rounded-none"/>)}
    </Page>
);
