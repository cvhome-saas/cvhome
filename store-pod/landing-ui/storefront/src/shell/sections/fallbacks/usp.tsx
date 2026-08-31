import type {SectionRenderProps} from '@store-front/theme';
import {items} from '../support';
import {EmptyOrHint, SectionHeading} from './shared';

const ICONS: Record<string, string> = {
    truck: '🚚', shield: '🛡️', refresh: '↩️', star: '⭐', headset: '🎧', gift: '🎁',
};

function Usp({section, preview}: SectionRenderProps) {
    const badges = items(section);
    if (badges.length === 0) {
        return <EmptyOrHint preview={preview} label="Trust badges — add one"/>;
    }
    return (
        <div>
            <SectionHeading title={section.text.title}/>
            <div className="grid grid-cols-2 gap-4 text-center md:grid-cols-4">
                {badges.map(badge => (
                    <div key={badge.id} className="flex flex-col items-center gap-1.5 p-3">
                        <span aria-hidden className="text-2xl">{ICONS[String(badge.props.icon)] ?? '✔️'}</span>
                        <span className="text-sm font-medium"><bdi dir="auto">{badge.text.title}</bdi></span>
                        {badge.text.body && (
                            <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
}

export const uspFallback = {row: Usp};
