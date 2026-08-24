'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {AnnouncementData} from '@store-front/types';

const KEY = 'beauty-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/** The stockroom notice: an ink strip, a primary "NOTE" tag at the start, the text in mono, a plate × at the end. */
export function Announcement({announcement}: { announcement: AnnouncementData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === announcement.code, () => false);
    if (hidden) return null;
    return (
        <div className="bg-foreground text-background">
            <div className="mx-auto flex max-w-wide items-stretch gap-3 px-gutter">
                <span aria-hidden className="tag my-1 hidden h-6 items-center self-center rounded-control ps-2 font-display text-[0.65rem] font-semibold uppercase tracking-wide sm:inline-flex">{t('ANNOUNCEMENT')}</span>
                <div className="min-w-0 flex-1 py-2 font-mono text-xs uppercase leading-snug tracking-wide [&_a]:underline" dangerouslySetInnerHTML={{__html: announcement.html}}/>
                <button type="button" aria-label={t('ANNOUNCEMENT_DISMISS')} onClick={() => dismiss(announcement.code)}
                        className="my-1 flex size-6 shrink-0 items-center justify-center self-center border border-background/60 hover:bg-background hover:text-foreground">
                    <XIcon className="size-3.5"/>
                </button>
            </div>
        </div>
    );
}
