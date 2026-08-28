import type {ContentData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PageHead} from '../components/PageHead';

export function Content({data}: PageProps<ContentData>) {
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <PageHead title={data.page.title}/>
            <article className="copy" dangerouslySetInnerHTML={{__html: data.html}}/>
        </PageShell>
    );
}
