import {Skeleton} from '@store-front/ui/skeleton';
import {Page, StripSkeleton} from './shared';

export const ContentSkeleton = () => (
    <Page width="narrow" className="flex flex-col gap-6 py-6 lg:py-8">
        <StripSkeleton className="h-7 w-40"/>
        <div className="sheet flex flex-col gap-4 p-10">
            <Skeleton className="h-12 w-2/3 rounded-none"/>
            {Array.from({length: 6}).map((_, i) => <Skeleton key={i} className="h-4 w-full rounded-none"/>)}
        </div>
    </Page>
);
