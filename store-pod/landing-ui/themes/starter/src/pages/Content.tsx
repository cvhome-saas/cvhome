import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="mb-6 text-3xl font-semibold tracking-tight">{data.page.description?.name}</h1>
                <div className="leading-relaxed [&_a]:underline [&_h2]:mb-3 [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-semibold [&_h3]:mb-2 [&_h3]:mt-6 [&_h3]:text-lg [&_h3]:font-semibold [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-4 [&_ul]:list-disc [&_ul]:ps-5"
                     dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
