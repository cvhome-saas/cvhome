import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-4 w-40"/>
        <div className="running-head"><Skeleton className="h-10 w-64"/><Skeleton className="h-4 w-20"/></div>
        <div className="flex gap-10">
            <div className="hidden w-56 flex-col gap-3 lg:flex"><Skeleton className="h-5 w-32"/><Skeleton className="h-4 w-40"/><Skeleton className="h-4 w-36"/><Skeleton className="h-4 w-28"/></div>
            <div className="flex flex-1 flex-col gap-4">
                <div className="flex items-center justify-between border-b pb-3"><Skeleton className="h-4 w-24"/><Skeleton className="h-8 w-40"/></div>
                <GridSkeleton/>
            </div>
        </div>
    </Page>
);
