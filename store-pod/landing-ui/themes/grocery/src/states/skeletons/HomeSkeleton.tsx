import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <>
        <Page className="pt-4 lg:pt-6">
            <div className="grid gap-3 lg:grid-cols-[2fr_3fr]">
                <Skeleton className="order-2 min-h-[16rem] rounded-card lg:order-1"/>
                <Skeleton className="order-1 aspect-[4/3] max-h-[38vh] w-full rounded-card sm:aspect-[16/9] sm:max-h-[46vh] lg:order-2 lg:aspect-auto lg:h-full lg:max-h-none"/>
            </div>
        </Page>
        <Page className="pt-section"><Skeleton className="mb-5 h-9 w-56"/><GridSkeleton count={5}/></Page>
    </>
);
