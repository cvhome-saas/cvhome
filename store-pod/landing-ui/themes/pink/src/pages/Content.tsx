import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PROSE} from '../components/prose';

export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8 lg:py-12">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="hair display mb-6 border-b-2 pb-4 text-3xl sm:text-4xl">{data.page.title}</h1>
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
