import {getTranslations} from 'next-intl/server';
import type {PageProps, PolicyData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PROSE} from '../components/prose';

export async function Policy({data}: PageProps<PolicyData>) {
    const t = await getTranslations('PAGE.POLICY');
    const {policy} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8 lg:py-12">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="display mb-3 text-3xl sm:text-4xl">{policy.heading}</h1>
                <p className="hair cover-line mb-8 border-b-2 pb-4 text-muted-foreground">
                    {t('VERSION', {version: policy.version})}
                    {policy.effectiveFrom && <> · {t('EFFECTIVE', {date: new Date(policy.effectiveFrom).toLocaleDateString()})}</>}
                </p>
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
