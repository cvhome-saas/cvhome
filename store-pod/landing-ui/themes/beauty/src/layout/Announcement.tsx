'use client'
import {useEffect, useState} from 'react';
import {useTranslations} from 'next-intl';
import {XIcon} from 'lucide-react';
import type {Box} from '@store-front/types';
import {parseDescription} from '@store-front/services/description-view-util';
import {Button} from '@store-front/ui/button';

const KEY = 'beauty-announcement-dismissed';

/** The merchant's `header-message` box. Dismissal is per session so it does not nag. */
export function Announcement({box}: { box: Box }) {
    const t = useTranslations('COMPONENTS.HEADER');
    const [hidden, setHidden] = useState(true);
    useEffect(() => {
        try { setHidden(sessionStorage.getItem(KEY) === box.code); } catch { setHidden(false); }
    }, [box.code]);
    if (hidden) return null;
    return (
        <div className="bg-primary text-primary-foreground">
            <div className="mx-auto flex max-w-content items-center gap-3 px-gutter py-2 text-sm">
                <div className="min-w-0 flex-1 text-center [&_a]:underline" dangerouslySetInnerHTML={{__html: parseDescription(box.description)}}/>
                <Button variant="ghost" size="icon-sm" className="shrink-0 text-primary-foreground hover:bg-primary-foreground/15 hover:text-primary-foreground"
                        aria-label={t('ANNOUNCEMENT_DISMISS')}
                        onClick={() => { try { sessionStorage.setItem(KEY, box.code); } catch {} setHidden(true); }}>
                    <XIcon/>
                </Button>
            </div>
        </div>
    );
}
