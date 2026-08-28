import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <Page>
        <div className="grid items-start gap-5 py-6 lg:grid-cols-[1fr_20rem] lg:gap-10 lg:py-8">
            <div className="flex flex-col gap-3"><Skeleton className="h-12 w-2/3"/><Skeleton className="h-4 w-40"/><Skeleton className="h-10 w-56"/></div>
            <Skeleton className="aspect-[21/9] w-full lg:aspect-[4/3]"/>
        </div>
        <Skeleton className="mb-5 h-9 w-full"/>
        <GridSkeleton count={6}/>
    </Page>
);
