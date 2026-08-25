import {Skeleton} from '@store-front/ui/skeleton';
import {GridSkeleton, Page, StripSkeleton} from './shared';

export const HomeSkeleton = () => (
    <>
        <Page width="wide" className="pt-5 lg:pt-8">
            <div className="wall grid grid-cols-2 gap-3 sm:gap-4 lg:grid-cols-12" aria-busy>
                <div className="sheet col-span-2 flex min-h-72 flex-col gap-4 p-6 lg:col-span-5"><Skeleton className="h-3 w-24 rounded-none"/><Skeleton className="h-16 w-4/5 rounded-none"/><Skeleton className="h-16 w-3/5 rounded-none"/><Skeleton className="mt-auto h-11 w-32 rounded-none"/></div>
                <div className="sheet col-span-1 aspect-[4/3] lg:col-span-4 lg:aspect-auto"><Skeleton className="size-full rounded-none"/></div>
                <div className="col-span-1 lg:col-span-3"><div className="sheet h-full"><Skeleton className="aspect-product w-full rounded-none"/></div></div>
            </div>
        </Page>
        <Page width="wide" className="pt-section"><div className="mb-5"><StripSkeleton/></div><GridSkeleton count={6}/></Page>
    </>
);
