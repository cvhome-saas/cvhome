import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <Page className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
        <Skeleton className="h-3.5 w-40 rounded-none"/>
        <Skeleton className="h-28 w-full rounded-none lg:h-36"/>
        <div className="flex gap-14">
            <div className="hidden w-56 shrink-0 flex-col gap-4 lg:flex">
                <Skeleton className="h-4 w-32 rounded-none"/><Skeleton className="h-4 w-40 rounded-none"/><Skeleton className="h-4 w-36 rounded-none"/>
            </div>
            <div className="min-w-0 flex-1"><GridSkeleton/></div>
        </div>
    </Page>
);
