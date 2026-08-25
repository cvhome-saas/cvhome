/**
 * What a record's code may be, and how to suggest one from a name.
 *
 * Letters, digits, hyphen and underscore. The pod does not validate this — it accepts anything a
 * `String` can hold — but a code with a slash or a space in it becomes a path segment on
 * `…/groups/{code}` and on the storefront's URLs, where it either breaks routing or arrives
 * percent-encoded and unrecognisable. Refusing it here is cheaper than discovering it there.
 */
export const CODE_PATTERN = /^[A-Za-z0-9_-]+$/;

/** Combining diacritics, stripped after NFKD so "Café" slugifies to "cafe" rather than "caf". */
const COMBINING_MARKS = /[\u0300-\u036f]/g;

/**
 * A code derived from a name.
 *
 * Nothing on the platform generates one and every create needs one, so the console offers the name
 * slugified, which is what seller-ui did. It stays editable: this is a suggestion, not a rule, and
 * a name written only in Arabic slugifies to nothing — which the caller falls back from.
 *
 * Shared rather than catalogue-local because "a coded record created from a typed name" is the
 * shape of every taxonomy the console has and every one it is likely to grow.
 */
export function slugify(value: string, maxLength = 60): string {
  return value
    .normalize('NFKD')
    .replace(COMBINING_MARKS, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, maxLength);
}
