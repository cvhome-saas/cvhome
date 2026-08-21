'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {Box} from '@store-front/types';
import {parseDescription} from '@store-front/services/description-view-util';
import {Button} from '@store-front/ui/button';

const KEY = 'starter-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/** The merchant's `header-message` box. Dismissal is per session so it does not nag. */
export function Announcement({box}: { box: Box }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === box.code, () => false);
    if (hidden) return null;
    return (
        <div className="bg-primary text-primary-foreground">
            <div className="mx-auto flex max-w-content items-center gap-3 px-gutter py-2 text-sm">
                <div className="min-w-0 flex-1 text-center [&_a]:underline" dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                <Button variant="ghost" size="icon-sm" className="shrink-0 text-primary-foreground hover:bg-primary-foreground/15 hover:text-primary-foreground"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => dismiss(box.code)}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
