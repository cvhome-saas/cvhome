import {getTranslations} from 'next-intl/server';
import type {PageProps, PolicyData} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

const PROSE = 'leading-relaxed [&_a]:underline [&_h2]:mb-3 [&_h2]:mt-8 [&_h2]:text-xl [&_h2]:font-semibold [&_h3]:mb-2 [&_h3]:mt-6 [&_h3]:text-lg [&_h3]:font-semibold [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-4 [&_ul]:list-disc [&_ul]:ps-5';

export async function Policy({data}: PageProps<PolicyData>) {
    const t = await getTranslations('PAGE.POLICY');
    const {policy} = data;
    return (
        <PageShell width="narrow" className="flex flex-col gap-6 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <article>
                <h1 className="mb-2 text-3xl font-semibold tracking-tight">{policy.heading}</h1>
                <p className="mb-6 text-sm text-muted-foreground">
                    {t('VERSION', {version: policy.version})}
                    {policy.effectiveFrom && <> · {t('EFFECTIVE', {date: new Date(policy.effectiveFrom).toLocaleDateString()})}</>}
                </p>
                <div className={PROSE} dangerouslySetInnerHTML={{__html: data.html}}/>
            </article>
        </PageShell>
    );
}
