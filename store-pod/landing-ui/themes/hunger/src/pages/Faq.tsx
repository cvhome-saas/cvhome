import {getTranslations} from 'next-intl/server';
import type {FaqData, PageProps} from '@store-front/theme';
import {PageShell} from '../components/PageShell';
import {Breadcrumbs} from '../components/Breadcrumbs';

/** The questions printed on the back of the sheet: ruled entries that open in place. */
export async function Faq({data}: PageProps<FaqData>) {
    const t = await getTranslations('PAGE.FAQ');
    const groups = data.faq.groups;
    return (
        <PageShell width="narrow" className="flex flex-col gap-8 py-6">
            <Breadcrumbs items={data.breadcrumbs}/>
            <header className="flex flex-col gap-2">
                <h1 className="press text-4xl leading-none">{t('TITLE')}</h1>
                <p className="text-sm text-muted-foreground">{t('SUBTITLE')}</p>
            </header>
            {groups.length === 0 ? (
                <p className="press py-12 text-center text-sm tracking-wide text-muted-foreground">{t('EMPTY')}</p>
            ) : groups.map(group => (
                <section key={group.key} id={group.key} className="flex flex-col">
                    <h2 className="press plate mb-2 px-3 py-1.5 text-xl leading-none">{group.name}</h2>
                    <div className="border-t border-border">
                        {group.entries.map(entry => (
                            <details key={entry.id} id={entry.slug} className="group border-b border-border py-3">
                                <summary className="cursor-pointer list-none marker:hidden [&::-webkit-details-marker]:hidden">
                                    <span className="flex items-baseline gap-2">
                                        <span className="font-semibold">{entry.question}</span>
                                        <span aria-hidden className="leader"/>
                                        <span aria-hidden className="press shrink-0 text-lg leading-none text-primary transition-transform duration-(--motion-base) group-open:rotate-45">+</span>
                                    </span>
                                </summary>
                                <div className="prose-hunger pt-2 text-sm text-muted-foreground" dangerouslySetInnerHTML={{__html: entry.answer}}/>
                            </details>
                        ))}
                    </div>
                </section>
            ))}
        </PageShell>
    );
}
