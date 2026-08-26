import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const HomeSkeleton = () => (
    <>
        <div className="flood hair border-b-2">
            <div className="mx-auto grid max-w-content gap-0 px-gutter lg:grid-cols-[7fr_5fr] lg:px-0">
                <div className="flex flex-col justify-center gap-5 py-10 lg:py-16 lg:ps-gutter lg:pe-10">
                    <Skeleton className="h-14 w-2/3 rounded-none bg-primary-foreground/25"/>
                    <Skeleton className="h-6 w-1/2 rounded-none bg-primary-foreground/25"/>
                    <Skeleton className="h-6 w-2/5 rounded-none bg-primary-foreground/25"/>
                    <Skeleton className="h-11 w-40 rounded-control bg-primary-foreground/25"/>
                </div>
                <Skeleton className="hair -mx-gutter min-h-64 rounded-none border-t-2 bg-primary-foreground/20 lg:mx-0 lg:min-h-[30rem] lg:border-s-2 lg:border-t-0"/>
            </div>
        </div>
        <Page className="py-section">
            <Skeleton className="mb-5 h-9 w-56 rounded-none"/>
            <GridSkeleton count={5}/>
        </Page>
    </>
);
