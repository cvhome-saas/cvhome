import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="signage mb-6 text-4xl lg:text-5xl">{data.page.title}</h1>
                <div className="prose-grocery"
                     dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
