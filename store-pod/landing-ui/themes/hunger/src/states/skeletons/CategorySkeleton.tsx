import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <Page className="flex flex-col gap-6 py-6">
        <Skeleton className="h-3 w-40"/>
        <Skeleton className="h-12 w-full"/>
        <div className="flex gap-10">
            <div className="hidden w-52 flex-col gap-3 lg:flex"><Skeleton className="h-5 w-32"/><Skeleton className="h-4 w-40"/><Skeleton className="h-4 w-36"/></div>
            <div className="flex-1"><GridSkeleton/></div>
        </div>
    </Page>
);
