/**
 * Where the product's written documentation lives.
 *
 * A constant rather than an environment value: it is the public docs site, the same one for every
 * deployment, and putting it in `environment.ts` would invite three copies that can disagree.
 *
 * Every link built from this opens in a new tab with `rel="noopener noreferrer"` — the console
 * holds a session, and a documentation link should never be able to reach back into the window
 * that opened it.
 */
export const DOCS_BASE_URL = 'https://cvhome-saas.github.io/';

/**
 * A documentation URL from a path relative to the site root.
 *
 * Leading slashes are tolerated so a caller can write either form; `new URL` would otherwise treat
 * `/products/` as an absolute path and silently drop any base sub-path the site later gains.
 */
export function docsUrl(path: string): string {
  return `${DOCS_BASE_URL}${path.replace(/^\/+/, '')}`;
}
