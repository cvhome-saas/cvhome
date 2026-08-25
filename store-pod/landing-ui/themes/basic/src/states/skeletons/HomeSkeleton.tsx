import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, HeadSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <>
        <Page className="pt-4 lg:pt-6">
            <div className="relative">
                <Skeleton className="aspect-[4/5] w-full rounded-none sm:aspect-[16/9] lg:aspect-[21/9]"/>
                <div className="plate flex flex-col gap-4 p-5 sm:p-6 lg:absolute lg:bottom-0 lg:start-0 lg:w-[min(40rem,48%)] lg:p-8">
                    <Skeleton className="h-14 w-3/4 bg-primary-foreground/20"/>
                    <Skeleton className="h-4 w-1/3 bg-primary-foreground/20"/>
                    <Skeleton className="h-11 w-36 bg-primary-foreground/20"/>
                </div>
            </div>
        </Page>
        <Page className="pt-section"><HeadSkeleton/><GridSkeleton count={4}/></Page>
    </>
);
