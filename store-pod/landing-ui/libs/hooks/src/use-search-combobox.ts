'use client'
import {type KeyboardEvent, useCallback, useEffect, useId, useMemo, useState} from 'react';

export interface UseSearchComboboxOptions {
    /** How many rows the dropdown is currently showing, including any trailing "see all" row. */
    optionCount: number;
    /** Whether the dropdown is open. Keys do nothing while it is closed, so Enter still submits the form. */
    open: boolean;
    /** Changing this clears the highlight — pass the query, so typing always starts the list again. */
    resetKey: string;
    /** Activate the highlighted row. The index is into the theme's own list, so it decides what row that is. */
    onSelect: (index: number) => void;
    /** Escape, or arrowing back past the top of the list. */
    onClose: () => void;
}

/**
 * Keyboard navigation for a search box that is already a combobox in markup.
 *
 * The boxes carry `role="combobox"` and `role="option"`, which is a promise to anyone using a keyboard or a
 * screen reader that the list is navigable. It was not: the only way to reach a suggestion was the mouse. This
 * is the behaviour behind that promise, in the shared hook rather than in twelve copies — a theme supplies the
 * row count and decides what activating row `i` means, and styles the highlight through `data-active`.
 *
 * Arrowing moves through -1 (nothing highlighted, the shopper's own text) and wraps at both ends, so there is
 * always a way back to what was typed — the same way a browser's address bar behaves.
 */
export function useSearchCombobox({optionCount, open, resetKey, onSelect, onClose}: UseSearchComboboxOptions) {
    const [activeIndex, setActiveIndex] = useState(-1);
    const listboxId = useId();

    const optionId = useCallback((index: number) => `${listboxId}-option-${index}`, [listboxId]);

    // A new query is a new list; keeping the old highlight would activate a row the shopper never saw.
    useEffect(() => setActiveIndex(-1), [resetKey]);
    useEffect(() => {
        if (!open) setActiveIndex(-1);
    }, [open]);
    // The list can shrink under the highlight when results come back.
    useEffect(() => {
        setActiveIndex(current => (current >= optionCount ? -1 : current));
    }, [optionCount]);

    /**
     * Keep the highlighted row visible. Read off the DOM by id rather than threading a ref per row through
     * every theme — the row is the theme's own element, and this is the one thing the hook needs from it.
     */
    useEffect(() => {
        if (activeIndex < 0 || typeof document === 'undefined') return;
        document.getElementById(optionId(activeIndex))?.scrollIntoView({block: 'nearest'});
    }, [activeIndex, optionId]);

    const move = useCallback((step: number) => {
        setActiveIndex(current => {
            if (optionCount === 0) return -1;
            // -1 is a real position: the text as typed. Cycling through it is what lets a shopper get back to it.
            const next = current + step;
            if (next < -1) return optionCount - 1;
            if (next >= optionCount) return -1;
            return next;
        });
    }, [optionCount]);

    const onKeyDown = useCallback((event: KeyboardEvent<HTMLInputElement>) => {
        if (!open) return;
        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                move(1);
                break;
            case 'ArrowUp':
                event.preventDefault();
                move(-1);
                break;
            case 'Home':
                if (optionCount > 0) {
                    event.preventDefault();
                    setActiveIndex(0);
                }
                break;
            case 'End':
                if (optionCount > 0) {
                    event.preventDefault();
                    setActiveIndex(optionCount - 1);
                }
                break;
            case 'Enter':
                // Only when a row is highlighted. Otherwise this is a plain submit and the form handles it,
                // which is what sends the shopper to the results page for exactly what they typed.
                if (activeIndex >= 0) {
                    event.preventDefault();
                    onSelect(activeIndex);
                }
                break;
            case 'Escape':
                event.preventDefault();
                setActiveIndex(-1);
                onClose();
                break;
            case 'Tab':
                onClose();
                break;
            default:
                break;
        }
    }, [activeIndex, move, onClose, onSelect, open, optionCount]);

    /**
     * Spread onto the input: which row is active, and the key handling.
     *
     * `aria-controls` is deliberately not here — a spread hides it from static analysis, and it is the one
     * attribute that says this input drives that listbox. Themes set it explicitly from `listboxId`.
     */
    const inputProps = useMemo(() => ({
        'aria-activedescendant': activeIndex >= 0 ? optionId(activeIndex) : undefined,
        onKeyDown,
    }), [activeIndex, onKeyDown, optionId]);

    /** Spread onto row `index`: its id, its selected state, and a hook for the theme's highlight styling. */
    const optionProps = useCallback((index: number) => ({
        id: optionId(index),
        role: 'option' as const,
        'aria-selected': activeIndex === index,
        'data-active': activeIndex === index ? 'true' : undefined,
        onMouseEnter: () => setActiveIndex(index),
    }), [activeIndex, optionId]);

    return {activeIndex, setActiveIndex, listboxId, optionId, inputProps, optionProps};
}
