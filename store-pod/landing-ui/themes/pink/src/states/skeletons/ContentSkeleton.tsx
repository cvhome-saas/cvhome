import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ContentSkeleton = () => (
    <Page width="narrow" className="flex flex-col gap-4 py-8 lg:py-12">
        <Skeleton className="h-4 w-40 rounded-none"/>
        <Skeleton className="mb-2 h-11 w-2/3 rounded-none"/>
        {Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-4 w-full rounded-none"/>)}
    </Page>
);
