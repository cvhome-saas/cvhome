import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <Page className="flex flex-col gap-6 py-8">
        <Skeleton className="h-4 w-40"/>
        <Skeleton className="h-9 w-64"/>
        <div className="flex gap-10">
            <div className="hidden w-56 flex-col gap-3 lg:flex"><Skeleton className="h-5 w-32"/><Skeleton className="h-4 w-40"/><Skeleton className="h-4 w-36"/></div>
            <div className="flex-1"><GridSkeleton/></div>
        </div>
    </Page>
);
