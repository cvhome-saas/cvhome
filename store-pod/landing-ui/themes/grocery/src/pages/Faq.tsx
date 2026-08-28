import {getTranslations} from 'next-intl/server';
import type {FaqData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

const PROSE = 'text-sm leading-relaxed text-muted-foreground [&_a]:underline [&_li]:mb-1 [&_ol]:list-decimal [&_ol]:ps-5 [&_p]:mb-2 [&_ul]:list-disc [&_ul]:ps-5';

export async function Faq({data}: PageProps<FaqData>) {
    const t = await getTranslations('PAGE.FAQ');
    const groups = data.faq.groups;
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-8">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-2">
                <h1 className="signage text-4xl lg:text-5xl">{t('TITLE')}</h1>
                <p className="text-muted-foreground">{t('SUBTITLE')}</p>
            </header>
            {groups.length === 0 ? (
                <p className="py-12 text-center text-muted-foreground">{t('EMPTY')}</p>
            ) : groups.map(group => (
                <section key={group.key} id={group.key} className="flex flex-col gap-3">
                    <h2 className="signage text-2xl">{group.name}</h2>
                    <div className="divide-y-2 overflow-hidden rounded-card border-2 bg-card">
                        {group.entries.map(entry => (
                            <details key={entry.id} id={entry.slug} className="group px-4 py-3">
                                <summary className="cursor-pointer list-none font-medium marker:hidden [&::-webkit-details-marker]:hidden">
                                    <span className="flex items-center justify-between gap-4">
                                        {entry.question}
                                        <span aria-hidden="true" className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                                    </span>
                                </summary>
                                <div className={`pt-2 ${PROSE}`} dangerouslySetInnerHTML={{__html: entry.answer}}/>
                            </details>
                        ))}
                    </div>
                </section>
            ))}
        </PageShell>
    );
}
