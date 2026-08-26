'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {AnnouncementData} from '@store-front/types';
import {Button} from '@store-front/ui/button';

const KEY = 'furniture-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/** The notice hung over the entrance: an enamel strip above the rail. Dismissal is per session. */
export function Announcement({announcement}: { announcement: AnnouncementData }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === announcement.code, () => false);
    if (hidden) return null;
    return (
        <div className="enamel-flat rounded-none">
            <div className="mx-auto flex max-w-content items-center gap-4 px-gutter py-2.5">
                <span aria-hidden className="sign hidden shrink-0 text-[0.625rem] opacity-75 sm:inline">{t('ANNOUNCEMENT')}</span>
                <div className="min-w-0 flex-1 text-sm [&_a]:underline" dangerouslySetInnerHTML={{__html: announcement.html}}/>
                <Button variant="ghost" size="icon-sm" className="shrink-0 text-current hover:bg-current/15 hover:text-current"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => dismiss(announcement.code)}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
