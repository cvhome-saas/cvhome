import {Skeleton} from '@store-front/ui/skeleton';
import {Page} from './shared';

export const ContentSkeleton = () => (
    <Page width="narrow" className="flex flex-col gap-4 py-6 lg:py-8">
        <Skeleton className="h-4 w-40"/>
        <Skeleton className="mb-4 h-12 w-2/3 border-b"/>
        {Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-4 w-full"/>)}
    </Page>
);
