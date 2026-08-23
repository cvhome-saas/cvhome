import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

/** A printed notice: quoted title in display caps, body in mono at reading measure. */
export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="q mb-6 font-display text-3xl font-bold uppercase leading-[0.95] tracking-tight [overflow-wrap:anywhere] sm:text-5xl">{data.page.description?.name}</h1>
                <div className="hazard mb-6 h-2" aria-hidden/>
                <div className="font-mono text-sm leading-relaxed [&_a]:underline [&_a]:underline-offset-4 [&_h2]:q [&_h2]:mb-3 [&_h2]:mt-8 [&_h2]:font-display [&_h2]:text-2xl [&_h2]:font-semibold [&_h2]:uppercase [&_h3]:mb-2 [&_h3]:mt-6 [&_h3]:font-display [&_h3]:text-lg [&_h3]:font-semibold [&_h3]:uppercase [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-4 [&_ul]:list-disc [&_ul]:ps-5"
                     dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
