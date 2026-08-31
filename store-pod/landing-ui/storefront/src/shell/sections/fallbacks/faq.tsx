import type {SectionRenderProps} from '@store-front/theme';
import {num} from '../support';
import {EmptyOrHint, SectionHeading} from './shared';

/** Native `<details>` accordion — server-rendered, zero client JS, keyboard accessible by default. */
function Faq({section, data, preview}: SectionRenderProps) {
    const entries = (data?.faq?.groups ?? []).flatMap(g => g.entries).slice(0, num(section.props.limit, 5));
    if (entries.length === 0) {
        return <EmptyOrHint preview={preview} label="FAQ — no published questions in this group"/>;
    }
    return (
        <div className="mx-auto max-w-2xl">
            <SectionHeading title={section.text.title}/>
            <div className="divide-y">
                {entries.map((entry, i) => (
                    <details key={i} className="group py-3">
                        <summary className="flex cursor-pointer list-none items-center justify-between gap-3 text-sm font-medium">
                            <bdi dir="auto">{entry.question}</bdi>
                            <span aria-hidden className="text-muted-foreground transition-transform group-open:rotate-45">+</span>
                        </summary>
                        <div className="pt-2 text-sm leading-relaxed text-muted-foreground"
                             dangerouslySetInnerHTML={{__html: entry.answer}}/>
                    </details>
                ))}
            </div>
        </div>
    );
}

export const faqFallback = {accordion: Faq};
