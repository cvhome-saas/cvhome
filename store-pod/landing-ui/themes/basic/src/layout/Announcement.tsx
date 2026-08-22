'use client'
import {useSyncExternalStore} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {Box} from '@store-front/types';
import {parseDescription} from '@store-front/services/description-view-util';
import {Button} from '@store-front/ui/button';

const KEY = 'basic-announcement-dismissed';
const listeners = new Set<() => void>();
const read = () => { try { return sessionStorage.getItem(KEY); } catch { return null; } };
const subscribe = (cb: () => void) => { listeners.add(cb); return () => { listeners.delete(cb); }; };
const dismiss = (code: string) => { try { sessionStorage.setItem(KEY, code); } catch {} listeners.forEach(l => l()); };

/** The merchant's `header-message` box as the catalogue's ink band above the masthead. Dismissal is per session. */
export function Announcement({box}: { box: Box }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const hidden = useSyncExternalStore(subscribe, () => read() === box.code, () => false);
    if (hidden) return null;
    return (
        <div className="bg-foreground text-background">
            <div className="mx-auto flex max-w-content items-center gap-3 px-gutter py-1.5 text-sm">
                <div className="min-w-0 flex-1 text-center [&_a]:underline" dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                <Button variant="ghost" size="icon-sm" className="shrink-0 text-background hover:bg-background/15 hover:text-background"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => dismiss(box.code)}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
