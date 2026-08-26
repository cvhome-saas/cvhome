'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {AnnouncementData} from '@store-front/types';
import {Button} from '@store-front/ui/button';

const KEY = 'pink-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/**
 * The band above the masthead: the merchant's `header-message` box, flooded and screentoned like a cover
 * strip. On a phone it drops the tracked caps — set that way a long message ran to four lines and pushed
 * the cover out of the first viewport — and its dismiss hides under a drawer's scrim (see tokens.css).
 */
export function Announcement({announcement}: { announcement: AnnouncementData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === announcement.code, () => false);
    if (hidden) return null;
    return (
        <div className="flood tone-light">
            <div className="mx-auto flex max-w-content items-center gap-3 px-gutter py-2">
                <div className="band-copy min-w-0 flex-1 text-center [&_a]:underline"
                     dangerouslySetInnerHTML={{__html: announcement.html}}/>
                <Button data-slot="announcement-dismiss" variant="ghost" size="icon-sm"
                        className="shrink-0 text-primary-foreground hover:bg-primary-foreground/20 hover:text-primary-foreground"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => dismiss(announcement.code)}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
