import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <Page className="flex flex-col gap-6 py-6 lg:py-8">
        <Skeleton className="h-4 w-40"/>
        <div className="flex flex-col gap-3 border-b-2 pb-4">
            <Skeleton className="h-11 w-72"/>
            <div className="flex gap-2">{Array.from({length: 4}).map((_, i) => <Skeleton key={i} className="h-11 w-28 rounded-control"/>)}</div>
        </div>
        <div className="flex gap-10">
            <div className="hidden w-56 flex-col gap-3 lg:flex"><Skeleton className="h-6 w-40"/><Skeleton className="h-4 w-44"/><Skeleton className="h-4 w-36"/></div>
            <div className="flex-1"><GridSkeleton/></div>
        </div>
    </Page>
);
