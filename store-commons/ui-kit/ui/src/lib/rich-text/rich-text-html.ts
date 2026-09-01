/**
 * The catalogue's HTML contract: what may be stored, and how anything else is reduced to it.
 *
 * Pure functions over a detached document — no Angular, no DOM of the live page — so the allow-list
 * can be tested on its own without mounting the editor.
 *
 * **Why not `DomSanitizer`.** Angular's sanitiser answers "is this safe to render", and its
 * allow-list is roughly sixty tags including `img`, `table`, `span` and inline `style`. A pasted
 * word-processor document survives it completely intact. This has to answer a narrower question —
 * "is this one of the ten tags this field is allowed to store" — and the two answers are not the
 * same. Angular's sanitiser still runs, independently, on anything ever bound with `[innerHTML]`.
 */

/** Every tag that may be stored, and the only attributes each may keep. */
const ALLOWED: Readonly<Record<string, readonly string[]>> = {
  p: [],
  br: [],
  h2: [],
  h3: [],
  strong: [],
  em: [],
  /*
   * `u` is readable but has no toolbar button. Underlined text that is not a link is a usability
   * defect, so the editor does not offer it — but data written by seller-ui's Quill may contain it,
   * and stripping it would silently change a seller's copy on first save.
   */
  u: [],
  ul: [],
  ol: [],
  li: [],
  a: ['href'],
  div: ['dir'],
};

/**
 * Tags rewritten to their allowed equivalent rather than unwrapped.
 *
 * `b` and `i` are what Firefox's `execCommand` emits where Chrome emits `strong` and `em`; `h1` and
 * `h4`–`h6` come from pasted documents and are folded into the two heading levels this editor has,
 * because a product description is not a page and does not own the `h1`.
 */
const RENAME: Readonly<Record<string, string>> = {
  b: 'strong',
  i: 'em',
  h1: 'h2',
  h4: 'h3',
  h5: 'h3',
  h6: 'h3',
};

/** Removed outright, with everything inside them. Everything else keeps its text. */
const DROP_WITH_SUBTREE: ReadonlySet<string> = new Set([
  'script',
  'style',
  'iframe',
  'object',
  'embed',
  'template',
]);

/** Schemes a link may use. Everything else — `javascript:`, `data:`, `vbscript:` — is refused. */
const SAFE_SCHEMES: ReadonlySet<string> = new Set(['http', 'https', 'mailto', 'tel']);

/**
 * Characters removed from a link target before its scheme is read.
 *
 * C0 and C7 controls, the non-breaking space and the Unicode line/paragraph separators. A browser
 * strips these when resolving a URL, so `java\u0000script:` is a scheme it will follow and a naive
 * prefix test will not see. Written as escapes so the file itself holds no control character.
 */
// The rule guards against typing a control character by accident. Here they are the subject.
// eslint-disable-next-line no-control-regex
const CONTROL_CHARACTERS = /[\u0000-\u001f\u007f\u00a0\u2028\u2029]/g;

/** Inline tags that mean nothing when they hold no text. Paste debris. */
const SHELL_TAGS: ReadonlySet<string> = new Set(['strong', 'em', 'u', 'a']);

/** Block-level tags, for deciding what counts as a stray at the top of a document. */
const BLOCK_TAGS: ReadonlySet<string> = new Set(['p', 'h2', 'h3', 'ul', 'ol', 'div']);

/**
 * A document reduced to the allowed tag set.
 *
 * Parsed with `DOMParser` rather than by assigning `innerHTML` to a detached element: the parsed
 * document is inert — no scripts run, no `img`/`iframe` fetches start, no `<base>` is resolved —
 * whereas a detached element's `innerHTML` still triggers resource loads in several engines.
 */
export function sanitizeHtml(html: string): string {
  if (!html.trim()) {
    return '';
  }
  const doc = new DOMParser().parseFromString(html, 'text/html');
  clean(doc.body, doc);
  promoteStrays(doc.body, doc);
  return doc.body.innerHTML;
}

/**
 * Whether a document says nothing.
 *
 * **Load-bearing, not cosmetic.** After any `execCommand` an empty editor holds `<p><br></p>`, which
 * is a non-empty string, so `Validators.required` would pass on a field the operator has not
 * written a word into.
 */
export function isEffectivelyEmpty(html: string): boolean {
  return (
    html
      .replace(/<br\s*\/?>/gi, '')
      .replace(/<[^>]*>/g, '')
      .replace(/&nbsp;|&#160;|\s/g, '').length === 0
  );
}

/** A document's outer `div[dir]` wrapper, if it has one, and the content inside it. */
export interface SplitDocument {
  /** `'rtl'`, `'ltr'`, or `null` for a document with no wrapper. */
  readonly wrapperDir: string | null;
  readonly inner: string;
}

/**
 * Separates a single wrapping `div[dir]` from the content it wraps.
 *
 * The seeded Arabic product descriptions are `<div dir="rtl">…</div>`, and that wrapper has to
 * survive editing. It cannot survive *inside* the contenteditable: `formatBlock` and
 * `insertOrderedList` routinely hoist nodes out of a single wrapping block, or duplicate it. So the
 * editor holds only the inner content and the wrapper is re-applied on the way out.
 *
 * Only an element that is the body's **one** child and carries an explicit `dir` counts. A bare
 * `<div>` with no `dir` is Safari's paragraph separator, not a wrapper, and is treated as content.
 */
export function splitWrapper(html: string): SplitDocument {
  if (!html.trim()) {
    return {wrapperDir: null, inner: ''};
  }
  const doc = new DOMParser().parseFromString(html, 'text/html');
  const children = [...doc.body.children];
  const only = children.length === 1 ? children[0] : null;

  if (
    only &&
    only.tagName.toLowerCase() === 'div' &&
    only.hasAttribute('dir') &&
    doc.body.textContent?.trim() === only.textContent?.trim()
  ) {
    const dir = only.getAttribute('dir')?.toLowerCase();
    return {
      wrapperDir: dir === 'rtl' || dir === 'ltr' ? dir : null,
      inner: only.innerHTML,
    };
  }
  return {wrapperDir: null, inner: doc.body.innerHTML};
}

/** The reverse: puts the wrapper back around content that has been edited. */
export function joinWrapper(inner: string, wrapperDir: string | null): string {
  if (!inner) {
    return '';
  }
  return wrapperDir ? `<div dir="${wrapperDir}">${inner}</div>` : inner;
}

/**
 * A link target, or `null` if it is not one this editor will store.
 *
 * Control characters, tabs, newlines and non-breaking spaces are stripped first: `java\nscript:` is
 * a scheme a browser will happily follow and a naive prefix test will happily miss. A target with
 * no scheme at all — `/men`, `#care`, `?size=9` — is relative and allowed.
 */
export function safeHref(raw: string | null): string | null {
  if (raw === null) {
    return null;
  }
  const value = raw.replace(CONTROL_CHARACTERS, '').trim();
  if (!value) {
    return null;
  }
  const scheme = /^([a-z][a-z0-9+.-]*):/i.exec(value);
  if (!scheme) {
    return value;
  }
  return SAFE_SCHEMES.has(scheme[1].toLowerCase()) ? value : null;
}

/* ------------------------------------------------------------------------ internals ---- */

function clean(parent: Element, doc: Document): void {
  // A snapshot, because the walk unwraps and removes as it goes.
  for (const node of [...parent.childNodes]) {
    if (node.nodeType === Node.TEXT_NODE) {
      continue;
    }
    if (node.nodeType !== Node.ELEMENT_NODE) {
      // Comments and processing instructions carry nothing a description needs.
      (node as ChildNode).remove();
      continue;
    }

    let element = node as Element;
    let tag = element.tagName.toLowerCase();

    if (DROP_WITH_SUBTREE.has(tag)) {
      element.remove();
      continue;
    }

    /*
     * A `div` is only ever kept for its direction. Anything else it might be — Safari's paragraph
     * separator, a wrapper from a paste, a `dir` this editor does not understand — is reduced:
     * a div holding blocks unwraps, and a div holding a line of text becomes that paragraph. This
     * is also why `splitWrapper` insists on an explicit, valid `dir` before calling something a
     * wrapper.
     */
    if (tag === 'div' && !keepsDirection(element)) {
      clean(element, doc);
      if (hasBlockChild(element)) {
        unwrap(element);
      } else {
        rename(element, 'p', doc);
      }
      continue;
    }

    if (RENAME[tag]) {
      element = rename(element, RENAME[tag], doc);
      tag = RENAME[tag];
    }

    const allowedAttributes = ALLOWED[tag];
    if (!allowedAttributes) {
      // Cleaned first, then unwrapped — never dropped. `<span>word</span>` has to keep "word".
      clean(element, doc);
      unwrap(element);
      continue;
    }

    // An allow-list, so `style`, `class`, `id`, every `on*` handler and every `data-*` all go
    // without any of them having to be named.
    for (const attribute of [...element.attributes]) {
      if (!allowedAttributes.includes(attribute.name.toLowerCase())) {
        element.removeAttribute(attribute.name);
      }
    }

    if (tag === 'a') {
      const href = safeHref(element.getAttribute('href'));
      if (href === null) {
        // A refused link becomes its own text rather than vanishing.
        clean(element, doc);
        unwrap(element);
        continue;
      }
      element.setAttribute('href', href);
    }

    clean(element, doc);

    if (SHELL_TAGS.has(tag) && !element.textContent?.trim() && !element.querySelector('br')) {
      unwrap(element);
    }
  }
}

/**
 * Wraps loose text and inline elements sitting directly in the body.
 *
 * A paste of "hello <strong>world</strong>" arrives as a text node and a `strong` with no block
 * around them. Left alone they render, but the next `formatBlock` has nothing to act on.
 */
function promoteStrays(body: HTMLElement, doc: Document): void {
  let paragraph: HTMLElement | null = null;

  for (const node of [...body.childNodes]) {
    const isBlock =
      node.nodeType === Node.ELEMENT_NODE && BLOCK_TAGS.has((node as Element).tagName.toLowerCase());

    if (isBlock) {
      paragraph = null;
      continue;
    }
    if (node.nodeType === Node.TEXT_NODE && !node.textContent?.trim()) {
      (node as ChildNode).remove();
      continue;
    }
    if (!paragraph) {
      paragraph = doc.createElement('p');
      body.insertBefore(paragraph, node);
    }
    paragraph.appendChild(node);
  }
}

/** Whether a `div` carries a direction worth keeping it for. Nothing else justifies a `div`. */
function keepsDirection(element: Element): boolean {
  const dir = element.getAttribute('dir')?.toLowerCase();
  return dir === 'rtl' || dir === 'ltr';
}

/** Whether an element holds a block, which decides whether reducing it unwraps or renames. */
function hasBlockChild(element: Element): boolean {
  return [...element.children].some((child) => BLOCK_TAGS.has(child.tagName.toLowerCase()));
}

/** Replaces an element with one of another tag, keeping its children. Attributes are re-filtered after. */
function rename(element: Element, tag: string, doc: Document): Element {
  const replacement = doc.createElement(tag);
  while (element.firstChild) {
    replacement.appendChild(element.firstChild);
  }
  element.replaceWith(replacement);
  return replacement;
}

/** Replaces an element with its own children. */
function unwrap(element: Element): void {
  const parent = element.parentNode;
  if (!parent) {
    return;
  }
  while (element.firstChild) {
    parent.insertBefore(element.firstChild, element);
  }
  element.remove();
}
