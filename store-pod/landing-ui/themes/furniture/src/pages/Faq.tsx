import {getTranslations} from 'next-intl/server';
import {PlusIcon} from 'lucide-react';
import type {FaqData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {PageHead} from '../components/PageHead';
import {SectionHeading} from '../components/SectionHeading';

/** The information desk: questions ruled like directory rows, answers folded under them. */
export async function Faq({data}: PageProps<FaqData>) {
    const t = await getTranslations('PAGE.FAQ');
    const groups = data.faq.groups;
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6 lg:gap-12 lg:py-10">
            <Breadcrumbs items={data.breadcrumbs}/>
            <PageHead title={t('TITLE')} meta={t('SUBTITLE')}/>
            {groups.length === 0 ? (
                <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
            ) : groups.map((group, i) => (
                <section key={group.key} id={group.key} className="flex flex-col">
                    <SectionHeading as="h2" plate={String(i + 1).padStart(2, '0')} title={group.name}/>
                    <div>
                        {group.entries.map(entry => (
                            <details key={entry.id} id={entry.slug} className="rule-brass group border-b py-4">
                                <summary className="flex cursor-pointer list-none items-center justify-between gap-4 marker:hidden [&::-webkit-details-marker]:hidden">
                                    <span className="text-base">{entry.question}</span>
                                    <PlusIcon aria-hidden className="size-4 shrink-0 text-muted-foreground transition-transform duration-(--motion-fast) group-open:rotate-45"/>
                                </summary>
                                <div className="copy mt-3 text-sm text-muted-foreground" dangerouslySetInnerHTML={{__html: entry.answer}}/>
                            </details>
                        ))}
                    </div>
                </section>
            ))}
        </PageShell>
    );
}
