/**
 * Puts text on the clipboard, on an insecure origin too.
 *
 * **Why this exists.** `navigator.clipboard` is gated to secure contexts — HTTPS, `localhost` or
 * `file:` — and the console is reached over plain HTTP on a named host (`console-ui.gateway.com`),
 * which is none of those. So the whole Clipboard API is simply `undefined` in development, and both
 * copy controls degraded to a toast telling the operator to select the value and copy it by hand.
 * Every copy button in the app was decorative. Same family as `crypto.randomUUID`, which
 * `toast.ts` already works around for the same reason.
 *
 * The fallback is the pre-Clipboard-API technique: put the text in an off-screen `<textarea>`,
 * select it, and let `document.execCommand('copy')` lift it out of the selection. It is deprecated
 * and it is also the only thing that works here, so it is the second attempt rather than the first.
 *
 * Returns whether the text actually landed, so a caller can report honestly either way — nothing
 * here throws.
 */
export async function copyText(value: string): Promise<boolean> {
  if (navigator.clipboard?.writeText) {
    try {
      await navigator.clipboard.writeText(value);
      return true;
    } catch {
      // Rejects when the document is not focused, among other reasons. Fall through and try the
      // selection route, which does not care about focus in the same way.
    }
  }
  return copyBySelection(value);
}

function copyBySelection(value: string): boolean {
  if (typeof document === 'undefined' || typeof document.execCommand !== 'function') {
    return false;
  }

  const carrier = document.createElement('textarea');
  carrier.value = value;
  /*
   * Off-screen rather than `display: none` or `hidden`: the selection APIs will not select inside
   * an element that is not rendered. `readonly` keeps a mobile keyboard from opening, and the
   * fixed position stops the page scrolling to it.
   */
  carrier.setAttribute('readonly', '');
  carrier.style.position = 'fixed';
  carrier.style.top = '0';
  carrier.style.insetInlineStart = '-9999px';
  carrier.style.opacity = '0';
  carrier.setAttribute('aria-hidden', 'true');

  const previous = document.activeElement;
  document.body.appendChild(carrier);

  try {
    carrier.select();
    carrier.setSelectionRange(0, value.length);
    return document.execCommand('copy');
  } catch {
    return false;
  } finally {
    carrier.remove();
    // Put focus back where the operator left it — usually the copy button they just pressed.
    if (previous instanceof HTMLElement) {
      previous.focus();
    }
  }
}
