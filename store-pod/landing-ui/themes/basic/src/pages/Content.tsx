import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

/** An information page: its title on a rule, the merchant's copy at reading measure. */
export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-6 lg:py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="display mb-8 border-b pb-4 text-4xl lg:text-5xl"><bdi dir="auto">{data.page.description?.name}</bdi></h1>
                <div className="prose-basic" dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
