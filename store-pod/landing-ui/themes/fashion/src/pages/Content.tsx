import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

/** A CMS page as one long sheet on the wall. */
export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article className="sheet sheen p-6 [--tilt:-0.3deg] sm:p-10">
                <h1 className="mb-6 font-display text-4xl uppercase leading-[0.9] [overflow-wrap:anywhere] sm:text-5xl" dir="auto"><bdi>{data.page.description?.name}</bdi></h1>
                <div className="max-w-[70ch] leading-relaxed [&_a]:underline [&_a]:decoration-primary [&_a]:decoration-2 [&_a]:underline-offset-4 [&_h2]:mb-3 [&_h2]:mt-8 [&_h2]:font-display [&_h2]:text-2xl [&_h2]:uppercase [&_h3]:mb-2 [&_h3]:mt-6 [&_h3]:font-display [&_h3]:text-xl [&_h3]:uppercase [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-4 [&_ul]:list-disc [&_ul]:ps-5"
                     dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
