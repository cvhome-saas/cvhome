import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page, StripSkeleton} from './shared';

export const CategorySkeleton = () => (
    <Page width="wide" className="flex flex-col gap-6 py-6 lg:py-8">
        <StripSkeleton className="h-7 w-48"/>
        <div className="sheet w-fit p-6"><Skeleton className="h-12 w-64 rounded-none"/><Skeleton className="mt-3 h-3 w-24 rounded-none"/></div>
        <div className="flex gap-8">
            <div className="sheet hidden w-60 flex-col gap-3 p-4 lg:flex"><Skeleton className="h-4 w-32 rounded-none"/><Skeleton className="h-4 w-40 rounded-none"/><Skeleton className="h-4 w-36 rounded-none"/></div>
            <div className="flex-1"><GridSkeleton/></div>
        </div>
    </Page>
);
