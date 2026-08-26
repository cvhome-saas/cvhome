import {getTranslations} from 'next-intl/server';
import type {FaqData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';
import {SectionHeading} from '../components/SectionHeading';

const ANSWER = 'text-sm leading-relaxed [&_a]:underline [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-2 [&_ul]:list-disc [&_ul]:ps-5';

export async function Faq({data}: PageProps<FaqData>) {
    const t = await getTranslations('PAGE.FAQ');
    const groups = data.faq.groups;
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-8 lg:py-12">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-2">
                <h1 className="display text-3xl sm:text-4xl">{t('TITLE')}</h1>
                <p className="text-muted-foreground">{t('SUBTITLE')}</p>
            </header>
            {groups.length === 0 ? (
                <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
            ) : groups.map((group, i) => (
                <section key={group.key} id={group.key} className="flex flex-col">
                    <SectionHeading as="h2" pagemark={String(i + 1).padStart(2, '0')} title={group.name}/>
                    <div className="hair border-t">
                        {group.entries.map(entry => (
                            <details key={entry.id} id={entry.slug} className="hair group border-b py-3">
                                <summary className="cursor-pointer list-none font-bold marker:hidden [&::-webkit-details-marker]:hidden">
                                    <span className="flex items-start justify-between gap-4">
                                        {entry.question}
                                        <span aria-hidden className="display shrink-0 text-lg transition-transform duration-(--motion-fast) group-open:rotate-45">+</span>
                                    </span>
                                </summary>
                                <div className={`pt-3 ${ANSWER}`} dangerouslySetInnerHTML={{__html: entry.answer}}/>
                            </details>
                        ))}
                    </div>
                </section>
            ))}
        </PageShell>
    );
}
