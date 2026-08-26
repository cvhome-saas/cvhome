'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {AnnouncementData} from '@store-front/types';
import {Button} from '@store-front/ui/button';

const KEY = 'hunger-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/**
 * The coupon strip along the top edge of the sheet — the merchant's `header-message` box, printed on the
 * second plate between two cut rules. Dismissal is per session so it does not nag.
 */
export function Announcement({announcement}: { announcement: AnnouncementData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === announcement.code, () => false);
    if (hidden) return null;
    return (
        <div className="plate border-y border-dashed border-primary-foreground/45">
            <div className="mx-auto flex max-w-content items-center gap-3 px-gutter py-1.5">
                <span aria-hidden className="press hidden shrink-0 text-xs tracking-wide opacity-75 sm:block">{t('ANNOUNCEMENT')}</span>
                <div className="min-w-0 flex-1 text-center text-sm [&_a]:underline [&_a]:underline-offset-2"
                     dangerouslySetInnerHTML={{__html: announcement.html}}/>
                <Button variant="ghost" size="icon-sm"
                        className="shrink-0 text-primary-foreground hover:bg-primary-foreground/15 hover:text-primary-foreground"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => dismiss(announcement.code)}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
