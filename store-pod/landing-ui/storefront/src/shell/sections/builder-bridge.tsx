'use client'
import {useEffect} from 'react';

/**
 * The builder's canvas contract, mounted only when the page renders a draft (preview token present). Origins
 * are allow-listed via NEXT_PUBLIC_BUILDER_ORIGINS (comma-separated); with none configured the bridge stays
 * silent, so a leaked preview URL never becomes a message channel.
 *
 * In:  {type: 'select' | 'hover' | 'scrollTo', sectionId}
 * Out: {type: 'ready'} on mount · {type: 'sectionClicked', sectionId} · {type: 'height', px} on resize.
 */
export function BuilderBridge() {
    useEffect(() => {
        const origins = (process.env.NEXT_PUBLIC_BUILDER_ORIGINS ?? '').split(',').map(o => o.trim()).filter(Boolean);
        if (origins.length === 0 || window.parent === window) return;
        const post = (message: Record<string, unknown>) => {
            for (const origin of origins) window.parent.postMessage(message, origin);
        };

        const outline = document.createElement('style');
        outline.textContent = `
            [data-section-id] { position: relative; }
            [data-section-id].builder-hover { outline: 1px dashed var(--color-primary, #10b981); outline-offset: -1px; }
            [data-section-id].builder-selected { outline: 2px solid var(--color-primary, #10b981); outline-offset: -2px; }
        `;
        document.head.appendChild(outline);

        const byId = (id: string) => document.querySelector<HTMLElement>(`[data-section-id="${CSS.escape(id)}"]`);
        const mark = (cls: string, id: string | null) => {
            document.querySelectorAll(`.${cls}`).forEach(el => el.classList.remove(cls));
            if (id) byId(id)?.classList.add(cls);
        };

        const onMessage = (event: MessageEvent) => {
            if (!origins.includes(event.origin)) return;
            const {type, sectionId} = (event.data ?? {}) as { type?: string; sectionId?: string };
            if (type === 'select') mark('builder-selected', sectionId ?? null);
            if (type === 'hover') mark('builder-hover', sectionId ?? null);
            if (type === 'scrollTo' && sectionId) byId(sectionId)?.scrollIntoView({behavior: 'smooth', block: 'start'});
        };

        const onClick = (event: MouseEvent) => {
            const section = (event.target as HTMLElement).closest<HTMLElement>('[data-section-id]');
            if (section?.dataset.sectionId) {
                post({type: 'sectionClicked', sectionId: section.dataset.sectionId});
            }
        };

        const observer = new ResizeObserver(() =>
            post({type: 'height', px: document.documentElement.scrollHeight}));
        observer.observe(document.documentElement);

        window.addEventListener('message', onMessage);
        document.addEventListener('click', onClick);
        post({type: 'ready'});
        return () => {
            observer.disconnect();
            window.removeEventListener('message', onMessage);
            document.removeEventListener('click', onClick);
            outline.remove();
        };
    }, []);
    return null;
}
