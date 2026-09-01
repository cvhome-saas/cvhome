import type {ComponentType} from 'react';
import {Gift, Headset, RotateCcw, ShieldCheck, Sparkles, Star, Truck} from 'lucide-react';
import type {SectionRenderProps} from '@store-front/theme';
import {items} from '../support';
import {EmptyOrHint, SectionHeading} from './shared';

/** Drawn icons in one stroke weight; the merchant picks by name in the inspector's select. */
const ICONS: Record<string, ComponentType<{className?: string; 'aria-hidden'?: boolean}>> = {
    truck: Truck, shield: ShieldCheck, refresh: RotateCcw, star: Star, headset: Headset, gift: Gift,
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
                {badges.map(badge => {
                    const Icon = ICONS[String(badge.props.icon)] ?? Sparkles;
                    return (
                        <div key={badge.id} className="flex flex-col items-center gap-1.5 p-3">
                            <Icon aria-hidden className="size-6 text-muted-foreground"/>
                            <span className="text-sm font-medium"><bdi dir="auto">{badge.text.title}</bdi></span>
                            {badge.text.body && (
                                <span className="text-xs text-muted-foreground"><bdi dir="auto">{badge.text.body}</bdi></span>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export const uspFallback = {row: Usp};
