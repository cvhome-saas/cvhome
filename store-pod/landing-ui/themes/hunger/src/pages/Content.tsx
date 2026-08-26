import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="press mb-6 border-b-2 border-foreground pb-2 text-4xl leading-none">{data.page.title}</h1>
                <div className="prose-hunger" dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
