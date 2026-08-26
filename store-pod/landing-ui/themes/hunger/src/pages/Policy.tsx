import {getTranslations} from 'next-intl/server';
import type {PageProps, PolicyData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

const PROSE = 'prose-hunger';

export async function Policy({data}: PageProps<PolicyData>) {
    const t = await getTranslations('PAGE.POLICY');
    const {policy} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="press mb-2 text-4xl leading-none">{policy.heading}</h1>
                <p className="press mb-6 border-b-2 border-foreground pb-2 text-xs tracking-wide text-muted-foreground">
                    {t('VERSION', {version: policy.version})}
                    {policy.effectiveFrom && <> · {t('EFFECTIVE', {date: new Date(policy.effectiveFrom).toLocaleDateString()})}</>}
                </p>
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
