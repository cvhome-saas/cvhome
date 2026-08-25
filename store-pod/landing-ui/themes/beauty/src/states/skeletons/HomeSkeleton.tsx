import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <>
        <Skeleton className="aspect-[16/7] w-full rounded-none sm:aspect-[21/8]"/>
        <Page className="py-section"><Skeleton className="mb-6 h-8 w-48"/><GridSkeleton count={4}/></Page>
    </>
);
