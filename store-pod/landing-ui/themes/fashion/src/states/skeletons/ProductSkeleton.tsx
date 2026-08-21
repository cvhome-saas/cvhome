import {Skeleton} from '@store-front/ui/skeleton';
import {Page, StripSkeleton} from './shared';

export const ProductSkeleton = () => (
    <Page width="wide" className="flex flex-col gap-8 py-6 lg:py-8">
        <StripSkeleton className="h-7 w-56"/>
        <div className="grid gap-6 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)] lg:gap-10">
            <div className="sheet"><Skeleton className="aspect-[4/5] w-full rounded-none"/></div>
            <div className="sheet flex flex-col gap-4 p-7">
                <Skeleton className="h-3 w-24 rounded-none"/><Skeleton className="h-10 w-4/5 rounded-none"/><Skeleton className="h-8 w-32 rounded-none"/>
                <Skeleton className="h-4 w-40 rounded-none"/><Skeleton className="mt-4 h-12 w-full rounded-none"/>
            </div>
        </div>
    </Page>
);
