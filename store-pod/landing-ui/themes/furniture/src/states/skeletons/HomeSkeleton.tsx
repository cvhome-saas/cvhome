import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <>
        <Page className="pt-6 lg:pt-10">
            <div className="grid items-stretch gap-5 lg:grid-cols-[minmax(0,1fr)_minmax(0,1.15fr)] lg:gap-8">
                <Skeleton className="h-[26rem] w-full rounded-none lg:h-[32rem]"/>
                <Skeleton className="h-60 w-full rounded-none lg:h-[32rem]"/>
            </div>
        </Page>
        <Page className="pt-section">
            <Skeleton className="mb-8 h-10 w-64 rounded-none"/>
            <GridSkeleton count={4}/>
        </Page>
    </>
);
