import Image from 'next/image';
import type {SectionRenderProps} from '@store-front/theme';
import {items, mediaUrl} from '../support';
import {EmptyOrHint, SectionHeading} from './shared';

function Testimonials({section, preview}: SectionRenderProps) {
    const quotes = items(section);
    if (quotes.length === 0) {
        return <EmptyOrHint preview={preview} label="Testimonials — add a quote"/>;
    }
    const plain = section.variant === 'quotes';
    return (
        <div>
            <SectionHeading title={section.text.title}/>
            <div className={plain ? 'mx-auto flex max-w-2xl flex-col gap-8' : 'grid gap-4 md:grid-cols-3'}>
                {quotes.map(quote => (
                    <figure key={quote.id} className={plain ? 'text-center' : 'rounded-lg border p-5'}>
                        <blockquote className="text-sm leading-relaxed">
                            <bdi dir="auto">“{quote.text.quote}”</bdi>
                        </blockquote>
                        {(quote.text.author || mediaUrl(quote.props)) && (
                            <figcaption className={`mt-3 flex items-center gap-2 text-xs text-muted-foreground ${plain ? 'justify-center' : ''}`}>
                                {mediaUrl(quote.props) && (
                                    <span className="relative size-7 overflow-hidden rounded-full bg-muted">
                                        <Image src={mediaUrl(quote.props)!} alt={quote.text.author ?? ''} fill className="object-cover"/>
                                    </span>
                                )}
                                <bdi dir="auto">{quote.text.author}</bdi>
                            </figcaption>
                        )}
                    </figure>
                ))}
            </div>
        </div>
    );
}

export const testimonialsFallback = {cards: Testimonials, quotes: Testimonials};
