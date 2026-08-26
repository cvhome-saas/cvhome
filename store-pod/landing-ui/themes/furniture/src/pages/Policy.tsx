import {getTranslations} from 'next-intl/server';
import type {PageProps, PolicyData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PageHead} from '../components/PageHead';

export async function Policy({data}: PageProps<PolicyData>) {
    const t = await getTranslations('PAGE.POLICY');
    const {policy} = data;
    const meta = [
        t('VERSION', {version: policy.version}),
        policy.effectiveFrom ? t('EFFECTIVE', {date: new Date(policy.effectiveFrom).toLocaleDateString()}) : undefined,
    ].filter(Boolean).join(' · ');
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <PageHead title={policy.heading} meta={meta}/>
            <article className="copy" dangerouslySetInnerHTML={{__html: data.html}}/>
        </PageShell>
    );
}
