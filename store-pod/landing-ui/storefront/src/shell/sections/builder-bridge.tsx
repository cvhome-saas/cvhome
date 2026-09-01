'use client'
import {useCallback, useEffect, useMemo, useRef, useState} from 'react';
import type {CSSProperties, PointerEvent as ReactPointerEvent} from 'react';
import {createPortal} from 'react-dom';

/**
 * The builder's canvas contract (protocol v2), mounted only when the page renders a draft (preview
 * token present). The console cannot reach into this document — so this bridge draws every canvas
 * affordance (selection/hover outlines, name tags, the floating toolbar, insertion line, add-here
 * zones) and reports *intents* back over postMessage. It never mutates the layout itself: the console
 * is the single writer, and the canvas shows the truth again after the next save-driven reload.
 *
 * Origins are allow-listed via NEXT_PUBLIC_BUILDER_ORIGINS (comma-separated); with none configured the
 * bridge stays silent, so a leaked preview URL never becomes a message channel. Every message carries
 * `v: 2`; unknown versions are dropped on both sides.
 *
 * In:  select | hover | scrollTo | guides {on} | locks {ids} | dragState {active, label} | dragOver {y}
 * Out: ready · height {px} · sectionClicked · sectionHovered · toolbar {action, sectionId}
 *      · dropTarget {beforeId|null} · reorder {sectionId, beforeId|null} · addHere {beforeId|null}
 */

interface SectionBox {
    id: string;
    kind: string;
    /** Document coordinates (rect + scrollY), so boxes stay put while the shopper viewport scrolls. */
    top: number;
    height: number;
}

type ToolbarAction = 'moveUp' | 'moveDown' | 'duplicate' | 'remove';

const EDGE_BAND_PX = 48;

/** How far sections part to make room at the insertion point — the gap the slot placeholder fills. */
const MAKE_ROOM_PX = 56;

const EDGE_SCROLL_STEP = 12;

const humanize = (kind: string) => kind.charAt(0).toUpperCase() + kind.slice(1);

export function BuilderBridge() {
    const origins = useMemo(
        () => (process.env.NEXT_PUBLIC_BUILDER_ORIGINS ?? '').split(',').map(o => o.trim()).filter(Boolean),
        []);
    const enabled = origins.length > 0 && typeof window !== 'undefined' && window.parent !== window;

    const [boxes, setBoxes] = useState<SectionBox[]>([]);
    const [selected, setSelected] = useState<string | null>(null);
    const [hovered, setHovered] = useState<string | null>(null);
    const [guides, setGuides] = useState(false);
    const [locks, setLocks] = useState<ReadonlySet<string>>(new Set());
    const [drag, setDrag] = useState<{label: string} | null>(null);
    const [dropBefore, setDropBefore] = useState<string | null | undefined>(undefined);
    const [reordering, setReordering] = useState<string | null>(null);
    /** A landed drop's boundary: the gap stays open with a settling slot until the reload. */
    const [pendingDrop, setPendingDrop] = useState<string | null | undefined>(undefined);
    const [updating, setUpdating] = useState(false);

    /** Latest drag pointer Y (viewport coords) for the auto-scroll loop. */
    const dragY = useRef<number | null>(null);
    const lastDropTarget = useRef<string | null | undefined>(undefined);

    const post = useCallback((message: Record<string, unknown>) => {
        for (const origin of origins) {
            window.parent.postMessage({v: 2, ...message}, origin);
        }
    }, [origins]);

    const measure = useCallback(() => {
        const next: SectionBox[] = [];
        document.querySelectorAll<HTMLElement>('[data-section-id]').forEach(el => {
            const rect = el.getBoundingClientRect();
            next.push({
                id: el.dataset.sectionId!,
                kind: el.dataset.sectionKind ?? 'section',
                top: rect.top + window.scrollY,
                height: rect.height,
            });
        });
        next.sort((a, b) => a.top - b.top);
        setBoxes(next);
        return next;
    }, []);

    /** The insertion point for a document-coordinate Y: id of the section the drop lands before. */
    const boundaryFor = useCallback((docY: number): string | null => {
        for (const box of boxes) {
            if (box.top + box.height / 2 > docY) {
                return box.id;
            }
        }
        return null;
    }, [boxes]);

    // ------------------------------------------------------------------------------- inbound protocol

    useEffect(() => {
        if (!enabled) return;
        const onMessage = (event: MessageEvent) => {
            if (!origins.includes(event.origin)) return;
            const data = (event.data ?? {}) as Record<string, unknown>;
            if (data.v !== 2) return; // versioned on both sides; anything unversioned is not the console
            const sectionId = typeof data.sectionId === 'string' ? data.sectionId : null;
            switch (data.type) {
                case 'select':
                    setSelected(sectionId);
                    setUpdating(false);
                    setPendingDrop(undefined);
                    break;
                case 'hover':
                    setHovered(sectionId);
                    break;
                case 'scrollTo':
                    if (sectionId) {
                        document.querySelector(`[data-section-id="${CSS.escape(sectionId)}"]`)
                            ?.scrollIntoView({behavior: 'smooth', block: 'start'});
                    }
                    break;
                case 'guides':
                    setGuides(data.on === true);
                    break;
                case 'locks':
                    setLocks(new Set(Array.isArray(data.ids) ? data.ids.filter(id => typeof id === 'string') : []));
                    break;
                case 'dragState':
                    if (data.active === true) {
                        setDrag({label: typeof data.label === 'string' ? data.label : ''});
                        setPendingDrop(undefined);
                        measure();
                    } else {
                        setDrag(null);
                        setDropBefore(undefined);
                        dragY.current = null;
                        lastDropTarget.current = undefined;
                        if (data.dropped === true) {
                            // the drop landed console-side; keep its gap open and shimmer until the
                            // saved document reloads with the real section standing in it
                            setPendingDrop(typeof data.beforeId === 'string' ? data.beforeId : null);
                            setUpdating(true);
                        }
                    }
                    break;
                case 'dragOver': {
                    if (typeof data.y !== 'number') break;
                    dragY.current = data.y;
                    const target = boundaryFor(data.y + window.scrollY);
                    setDropBefore(target);
                    if (target !== lastDropTarget.current) {
                        lastDropTarget.current = target;
                        post({type: 'dropTarget', beforeId: target});
                    }
                    break;
                }
            }
        };
        window.addEventListener('message', onMessage);
        return () => window.removeEventListener('message', onMessage);
    }, [enabled, origins, post, measure, boundaryFor]);

    // -------------------------------------------------------------------------- measurement & height

    useEffect(() => {
        if (!enabled) return;
        // measured on the next frame: a synchronous setState inside an effect cascades renders
        const raf = requestAnimationFrame(measure);
        const observer = new ResizeObserver(() => {
            post({type: 'height', px: document.documentElement.scrollHeight});
            measure();
        });
        observer.observe(document.documentElement);
        return () => {
            cancelAnimationFrame(raf);
            observer.disconnect();
        };
    }, [enabled, measure, post]);

    // ------------------------------------------------------------------------------ click & hover out

    useEffect(() => {
        if (!enabled) return;
        const onClick = (event: MouseEvent) => {
            // the canvas is a selection surface, not a browsing session: swallow the page's own
            // links and buttons (capture phase, before the theme's handlers) so a click can never
            // navigate the draft away or mutate a cart from inside the builder
            event.preventDefault();
            event.stopPropagation();
            const section = (event.target as HTMLElement).closest<HTMLElement>('[data-section-id]');
            if (section?.dataset.sectionId) {
                post({type: 'sectionClicked', sectionId: section.dataset.sectionId});
            }
        };
        let lastHover: string | null = null;
        const onOver = (event: MouseEvent) => {
            const id = (event.target as HTMLElement).closest<HTMLElement>('[data-section-id]')
                ?.dataset.sectionId ?? null;
            if (id !== lastHover) {
                lastHover = id;
                setHovered(id);
                post({type: 'sectionHovered', sectionId: id});
            }
        };
        document.addEventListener('click', onClick, true);
        document.addEventListener('mouseover', onOver);
        post({type: 'ready'});
        return () => {
            document.removeEventListener('click', onClick, true);
            document.removeEventListener('mouseover', onOver);
        };
    }, [enabled, post]);

    // ------------------------------------------------------------------------------------ auto-scroll

    useEffect(() => {
        if (!enabled || !drag) return;
        let raf = 0;
        const tick = () => {
            const y = dragY.current;
            if (y != null) {
                if (y < EDGE_BAND_PX) {
                    window.scrollBy(0, -EDGE_SCROLL_STEP);
                } else if (y > window.innerHeight - EDGE_BAND_PX) {
                    window.scrollBy(0, EDGE_SCROLL_STEP);
                }
            }
            raf = requestAnimationFrame(tick);
        };
        raf = requestAnimationFrame(tick);
        return () => cancelAnimationFrame(raf);
    }, [enabled, drag]);

    // -------------------------------------------------------------------------------- canvas reorder

    /** Tears down the active reorder's window listeners; also runs on unmount so none can leak. */
    const reorderCleanup = useRef<(() => void) | null>(null);
    useEffect(() => () => reorderCleanup.current?.(), []);

    const startReorder = useCallback((sectionId: string, down: PointerEvent | ReactPointerEvent) => {
        down.preventDefault();
        setReordering(sectionId);
        // the drag computes boundaries from this measurement, not the async state it also feeds
        const fresh = measure();
        const boundaryIn = (docY: number): string | null => {
            for (const box of fresh) {
                if (box.top + box.height / 2 > docY) return box.id;
            }
            return null;
        };
        // capture keeps move/up flowing to us even when the pointer leaves the iframe's viewport
        const target = down.target as Element | null;
        try {
            target?.setPointerCapture?.(down.pointerId);
        } catch {
            // capture is an optimization; a target that refuses it still gets window-level events
        }
        let before: string | null | undefined;
        const onMove = (move: PointerEvent) => {
            dragY.current = move.clientY;
            before = boundaryIn(move.clientY + window.scrollY);
            setDropBefore(before);
        };
        const finish = (commit: boolean) => {
            window.removeEventListener('pointermove', onMove);
            window.removeEventListener('pointerup', onUp);
            window.removeEventListener('pointercancel', onCancel);
            window.removeEventListener('keydown', onKey);
            reorderCleanup.current = null;
            setReordering(null);
            setDropBefore(undefined);
            dragY.current = null;
            if (commit && before !== undefined && before !== sectionId) {
                setUpdating(true);
                post({type: 'reorder', sectionId, beforeId: before});
            }
        };
        const onUp = () => finish(true);
        // a browser gesture (touch scroll) taking the pointer must not leave the drag stuck
        const onCancel = () => finish(false);
        const onKey = (event: KeyboardEvent) => {
            if (event.key === 'Escape') finish(false);
        };
        window.addEventListener('pointermove', onMove);
        window.addEventListener('pointerup', onUp);
        window.addEventListener('pointercancel', onCancel);
        window.addEventListener('keydown', onKey);
        reorderCleanup.current = () => finish(false);
    }, [measure, post]);

    // reorder shares the edge auto-scroll with library drags
    useEffect(() => {
        if (!enabled || !reordering) return;
        let raf = 0;
        const tick = () => {
            const y = dragY.current;
            if (y != null) {
                if (y < EDGE_BAND_PX) window.scrollBy(0, -EDGE_SCROLL_STEP);
                else if (y > window.innerHeight - EDGE_BAND_PX) window.scrollBy(0, EDGE_SCROLL_STEP);
            }
            raf = requestAnimationFrame(tick);
        };
        raf = requestAnimationFrame(tick);
        return () => cancelAnimationFrame(raf);
    }, [enabled, reordering]);

    // the overlay anchors to the document body via a portal — an ancestor with its own positioning
    // context (any theme wrapper) must never own these coordinates
    const [mounted, setMounted] = useState(false);
    useEffect(() => {
        const raf = requestAnimationFrame(() => setMounted(true));
        return () => cancelAnimationFrame(raf);
    }, []);

    // a lost console answer must not leave the shimmer (or a settling slot) on forever
    useEffect(() => {
        if (!updating) return;
        const timer = setTimeout(() => {
            setUpdating(false);
            setPendingDrop(undefined);
        }, 8000);
        return () => clearTimeout(timer);
    }, [updating]);

    // ----------------------------------------------------------------------------------- make room
    // While a drag hovers a boundary (or a drop is settling), the sections below it physically part
    // by MAKE_ROOM_PX — the page itself shows where the new block will stand, not just a line.
    const shiftBoundary = drag || reordering ? dropBefore : pendingDrop;
    useEffect(() => {
        if (!enabled) return;
        const els = [...document.querySelectorAll<HTMLElement>('[data-section-id]')];
        const active = shiftBoundary !== undefined;
        const at = shiftBoundary === null
            ? els.length
            : els.findIndex(el => el.dataset.sectionId === shiftBoundary);
        els.forEach((el, index) => {
            el.style.transition = 'transform 0.18s ease, opacity 0.18s ease';
            el.style.transform = active && at >= 0 && index >= at ? `translateY(${MAKE_ROOM_PX}px)` : '';
            el.style.opacity = reordering && el.dataset.sectionId === reordering ? '0.35' : '';
        });
        return () => {
            els.forEach(el => {
                el.style.transform = '';
                el.style.opacity = '';
                el.style.transition = '';
            });
        };
    }, [enabled, shiftBoundary, reordering]);

    if (!enabled || !mounted) return null;

    const lineTop = (beforeId: string | null | undefined): number | null => {
        if (beforeId === undefined) return null;
        if (beforeId === null) {
            const last = boxes[boxes.length - 1];
            return last ? last.top + last.height : null;
        }
        const box = boxes.find(b => b.id === beforeId);
        return box ? box.top : null;
    };

    const slotTop = shiftBoundary !== undefined ? lineTop(shiftBoundary) : null;
    const slotLabel = pendingDrop !== undefined ? '' : drag?.label
        ?? (reordering ? humanize(boxes.find(b => b.id === reordering)?.kind ?? '') : '');
    const showZones = guides || !!drag;
    const docHeight = boxes.length > 0
        ? Math.max(...boxes.map(b => b.top + b.height))
        : 0;

    const toolbarFor = selected && !locks.has(selected) && !drag
        ? boxes.find(b => b.id === selected) : undefined;
    const selectedIndex = boxes.findIndex(b => b.id === selected);

    const act = (action: ToolbarAction) => {
        if (!selected) return;
        setUpdating(true);
        post({type: 'toolbar', action, sectionId: selected});
    };

    return createPortal(
        <div aria-hidden data-builder-overlay style={{position: 'absolute', insetInlineStart: 0, top: 0, width: '100%', height: docHeight, pointerEvents: 'none', zIndex: 50}}>
            {boxes.map(box => {
                const isSelected = box.id === selected;
                const isHovered = box.id === hovered;
                const outlined = isSelected || isHovered || guides;
                const locked = locks.has(box.id);
                return (
                    <div key={box.id} style={{position: 'absolute', insetInlineStart: 0, width: '100%', top: box.top, height: box.height}}>
                        {outlined && (
                            <div style={{
                                position: 'absolute', inset: 0,
                                outline: isSelected ? '2px solid var(--color-primary, #10b981)' : '1px dashed var(--color-primary, #10b981)',
                                outlineOffset: -2, opacity: isSelected ? 1 : 0.6,
                                animation: isSelected ? 'builder-settle 0.35s ease' : undefined,
                            }}/>
                        )}
                        {(outlined || locked) && (
                            <span style={{
                                position: 'absolute', top: 0, insetInlineStart: 0,
                                padding: '2px 8px', fontSize: 10, fontWeight: 700, letterSpacing: '0.05em',
                                textTransform: 'uppercase', color: '#fff', whiteSpace: 'nowrap',
                                background: isSelected ? 'var(--color-primary, #10b981)' : 'color-mix(in srgb, var(--color-primary, #10b981) 70%, transparent)',
                            }}>
                                {locked ? '🔒 ' : ''}{humanize(box.kind)}
                            </span>
                        )}
                    </div>
                );
            })}

            {toolbarFor && (
                <div style={{
                    position: 'absolute', top: Math.max(2, toolbarFor.top - 14), insetInlineEnd: 8,
                    display: 'flex', gap: 2, padding: 3, borderRadius: 7,
                    background: 'var(--color-foreground, #0f172a)', pointerEvents: 'auto',
                    boxShadow: '0 4px 12px rgb(0 0 0 / 0.25)',
                }}>
                    <button type="button" tabIndex={-1} aria-label="Drag to reorder" title="Drag to reorder"
                            onPointerDown={e => startReorder(toolbarFor.id, e)}
                            style={toolbarButton('grab')}>⠿</button>
                    <button type="button" tabIndex={-1} aria-label="Move up" disabled={selectedIndex <= 0}
                            onClick={() => act('moveUp')} style={toolbarButton()}>↑</button>
                    <button type="button" tabIndex={-1} aria-label="Move down" disabled={selectedIndex === boxes.length - 1}
                            onClick={() => act('moveDown')} style={toolbarButton()}>↓</button>
                    <button type="button" tabIndex={-1} aria-label="Duplicate" onClick={() => act('duplicate')}
                            style={toolbarButton()}>⧉</button>
                    <button type="button" tabIndex={-1} aria-label="Remove" onClick={() => act('remove')}
                            style={{...toolbarButton(), color: '#fca5a5'}}>✕</button>
                </div>
            )}

            {showZones && [...boxes.map(b => ({key: b.id, beforeId: b.id as string | null, top: b.top})),
                {key: '__end', beforeId: null, top: docHeight}].map(zone => (
                <button key={zone.key} type="button" tabIndex={-1}
                        onClick={() => post({type: 'addHere', beforeId: zone.beforeId})}
                        style={{
                            position: 'absolute', top: zone.top - 10, insetInlineStart: '10%', width: '80%', height: 20,
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            border: 'none', background: 'transparent', cursor: 'pointer', pointerEvents: 'auto',
                            color: 'var(--color-primary, #10b981)', fontSize: 10, fontWeight: 700,
                            opacity: drag ? 0 : undefined,
                        }}>
                    <span style={{flex: 1, borderTop: '1.5px dashed currentColor', opacity: 0.5}}/>
                    <span style={{padding: '0 8px'}}>＋</span>
                    <span style={{flex: 1, borderTop: '1.5px dashed currentColor', opacity: 0.5}}/>
                </button>
            ))}

            {slotTop != null && (
                <div style={{
                    position: 'absolute', top: slotTop + 8, insetInlineStart: '4%', width: '92%',
                    height: MAKE_ROOM_PX - 16, borderRadius: 10,
                    border: '2px dashed var(--color-primary, #10b981)',
                    background: 'color-mix(in srgb, var(--color-primary, #10b981) 8%, transparent)',
                    display: 'flex', alignItems: 'center', justifyContent: 'center', gap: 8,
                    color: 'var(--color-primary, #10b981)', fontSize: 11, fontWeight: 700,
                    letterSpacing: '0.06em', textTransform: 'uppercase',
                    animation: pendingDrop !== undefined ? 'builder-shimmer 1s linear infinite' : undefined,
                }}>
                    {slotLabel || '···'}
                </div>
            )}

            {(drag || updating) && (
                <div style={{
                    position: 'fixed', top: 0, insetInlineStart: 0, width: '100%', height: 3, zIndex: 60,
                    background: 'var(--color-primary, #10b981)', opacity: 0.85,
                    animation: updating ? 'builder-shimmer 1s linear infinite' : undefined,
                }}/>
            )}
            <style>{`@keyframes builder-shimmer { 0% {opacity: .35} 50% {opacity: .9} 100% {opacity: .35} }
@keyframes builder-settle { from {opacity: 0; outline-offset: 6px} to {outline-offset: -2px} }
@media (prefers-reduced-motion: reduce) { [data-builder-overlay] * { animation: none !important } }`}</style>
        </div>,
        document.body,
    );
}

function toolbarButton(cursor = 'pointer'): CSSProperties {
    return {
        width: 26, height: 24, display: 'flex', alignItems: 'center', justifyContent: 'center',
        border: 'none', borderRadius: 4, background: 'transparent', color: '#fff', cursor,
        fontSize: 12, lineHeight: 1,
    };
}
