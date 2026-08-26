import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page} from './shared';

export const CategorySkeleton = () => (
    <>
        <div className="flood hair border-b-2">
            <Page className="flex flex-col gap-5 py-8 lg:py-12">
                <Skeleton className="h-4 w-48 rounded-none bg-primary-foreground/25"/>
                <Skeleton className="h-12 w-80 max-w-full rounded-none bg-primary-foreground/25"/>
                <Skeleton className="h-5 w-64 max-w-full rounded-none bg-primary-foreground/25"/>
            </Page>
        </div>
        <Page className="flex gap-10 py-8">
            <div className="hidden w-56 shrink-0 flex-col gap-3 lg:flex">
                <Skeleton className="h-7 w-32 rounded-none"/><Skeleton className="h-4 w-40 rounded-none"/><Skeleton className="h-4 w-36 rounded-none"/>
            </div>
            <div className="min-w-0 flex-1"><GridSkeleton/></div>
        </Page>
    </>
);
