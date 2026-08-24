'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {AnnouncementData} from '@store-front/types';

const KEY = 'fashion-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/** The merchant's `header-message` box as a strip of day-glo tape across the top of the wall. Dismissal is per session. */
export function Announcement({announcement}: { announcement: AnnouncementData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === announcement.code, () => false);
    if (hidden) return null;
    return (
        <div className="tape bg-primary text-primary-foreground">
            <div className="mx-auto flex max-w-wide items-center gap-3 px-gutter py-1.5">
                <div className="min-w-0 flex-1 text-center font-display text-sm uppercase tracking-wide [&_a]:underline [&_a]:underline-offset-2" dangerouslySetInnerHTML={{__html: announcement.html}}/>
                <button type="button" className="flex size-7 shrink-0 items-center justify-center hover:bg-primary-foreground/15" aria-label={t('ANNOUNCEMENT_DISMISS')} onClick={() => dismiss(announcement.code)}>
                    <XIcon className="size-4"/>
                </button>
            </div>
        </div>
    );
}
