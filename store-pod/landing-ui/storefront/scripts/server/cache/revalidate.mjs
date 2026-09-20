/**
 * Refreshes a stale page by asking this same server for it again, with the headers that make the render that host's
 * store's (`FORWARDED_HEADERS`) and the revalidate marker, and discarding the answer: the cache keeps the copy on the
 * way out. A request of the cache's own, rather than a render conjured in-process, because the page must be exactly
 * what a shopper would get: the proxy's rewrite into the theme's tree, next-intl, `headers()`. `port` is read on
 * each call, since the server is listening only after start.
 */
import http from 'node:http';
import {randomBytes} from 'node:crypto';
import {FORWARDED_HEADERS, REVALIDATE_HEADER} from './config.mjs';

const LOOPBACK = new Set(['127.0.0.1', '::1', '::ffff:127.0.0.1']);

/**
 * The marker's value: a secret of this process, so that only its own refreshes are taken as such. An address check
 * would not do: under Docker on macOS, spg's connections arrive from 127.0.0.1 as well.
 */
export const REVALIDATE_TOKEN = randomBytes(16).toString('hex');

export function loopbackRevalidator(port) {
    return req => new Promise((resolve, reject) => {
        const headers = {[REVALIDATE_HEADER]: REVALIDATE_TOKEN};
        for (const name of FORWARDED_HEADERS) {
            if (req.headers[name] !== undefined) {
                headers[name] = req.headers[name];
            }
        }
        const refresh = http.request({host: '127.0.0.1', port: port(), path: req.url, method: 'GET', headers},
            response => {
                response.resume();
                response.once('end', resolve);
                response.once('error', reject);
            });
        refresh.once('error', reject);
        refresh.end();
    });
}

/**
 * Whether the request came straight from this host: a loopback address and no proxy in between (spg's Caddy adds
 * `X-Forwarded-For` to everything it forwards). The stats route is open from here.
 */
export function fromLoopback(req) {
    return LOOPBACK.has(req.socket?.remoteAddress ?? '') && req.headers['x-forwarded-for'] === undefined;
}

/** Whether this is the cache's own refresh: the marker carries this process's secret; anything else is ignored. */
export function isRevalidation(req) {
    return req.headers[REVALIDATE_HEADER] === REVALIDATE_TOKEN;
}
