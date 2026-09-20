/**
 * A copy of what Next sends, taken as it streams, and the rules for whether the copy may be kept.
 *
 * Never kept: anything but a 200; an HTML body that did not reach its `</html>` (Next cannot change the status once
 * the shell is out, so a render that failed halfway still says 200); a render one of whose backend reads was
 * aborted or timed out (`degraded`: a page missing a section, which is what libs/services' orUndefined turns a
 * failed read into); a response setting a cookie other than next-intl's NEXT_LOCALE; a response that varies on a
 * header the key does not carry; a body over `maxEntryBytes`.
 *
 * `writeHead` is hooked so that headers given inline are folded into `setHeader`, which makes them visible to the
 * edge-header enforcement (`onHead`) and to the copy alike; Node's implicit head on the first write goes through
 * the same hook.
 */
import {NEXT_LOCALE, NEXT_RSC_HEADERS} from './config.mjs';

/** Response headers never replayed: they describe one connection, or are recomputed for the copy being sent. */
const HOP_HEADERS = new Set(['connection', 'keep-alive', 'transfer-encoding', 'date', 'content-length', 'set-cookie',
    'x-storefront-cache', 'x-storefront-cache-class', 'x-storefront-cache-key', 'x-storefront-cache-reason']);

/** A response may vary on Next's RSC headers (bypassed) and on the store headers (keyed); on anything else it is not kept. */
const KEYED_VARY = new Set([...NEXT_RSC_HEADERS, 'store-id', 'theme', 'color-theme', 'default-language', 'supported-languages']);

/**
 * Hooks {@code res} and resolves, once the response has finished or closed, with the entry to keep or undefined.
 *
 * @param onHead called with the status the moment the head is written, before it goes out
 */
export function captureRender(res, {maxEntryBytes, degraded = () => false, now = Date.now, cls, ttlMs, staleMs, onHead}) {
    const chunks = [];
    let size = 0;
    const {write, end, writeHead} = res;
    const keep = (chunk, encoding) => {
        if (chunk === undefined || chunk === null || typeof chunk === 'function') {
            return;
        }
        const buffer = typeof chunk === 'string'
            ? Buffer.from(chunk, typeof encoding === 'string' ? encoding : 'utf8')
            : Buffer.from(chunk);
        size += buffer.length;
        if (size <= maxEntryBytes) {
            chunks.push(buffer);
        }
    };
    res.writeHead = function (status, ...rest) {
        const at = rest.findIndex(arg => arg && typeof arg === 'object');
        if (at >= 0) {
            for (const [name, value] of Object.entries(headersOf(rest[at]))) {
                this.setHeader(name, value);
            }
            rest.splice(at, 1);
        }
        onHead?.(status);
        return writeHead.call(this, status, ...rest);
    };
    res.write = function (chunk, encoding, ...rest) {
        keep(chunk, encoding);
        return write.call(this, chunk, encoding, ...rest);
    };
    res.end = function (chunk, encoding, ...rest) {
        keep(chunk, encoding);
        return end.call(this, chunk, encoding, ...rest);
    };
    return new Promise(resolve => {
        res.once('finish', () => {
            const headers = lower(res.getHeaders());
            const body = Buffer.concat(chunks);
            resolve(keepable(res, headers, size, body, maxEntryBytes, degraded) ? {
                status: res.statusCode,
                headers: replayable(headers),
                locale: localeCookieOf(headers),
                body,
                cls,
                storedAt: now(),
                ttlMs,
                staleMs,
            } : undefined);
        });
        res.once('close', () => resolve(undefined));
    });
}

function keepable(res, headers, size, body, maxEntryBytes, degraded) {
    if (res.statusCode !== 200 || size > maxEntryBytes || degraded(res)) {
        return false;
    }
    const cookies = [].concat(headers['set-cookie'] ?? []);
    if (cookies.some(cookie => !String(cookie).startsWith(`${NEXT_LOCALE}=`))) {
        return false;
    }
    const vary = String(headers.vary ?? '').toLowerCase().split(',').map(name => name.trim()).filter(Boolean);
    if (!vary.every(name => KEYED_VARY.has(name))) {
        return false;
    }
    return !String(headers['content-type'] ?? '').startsWith('text/html') || whole(body);
}

/** An HTML document Next finished: a stream that failed after the shell was flushed stops short of this. */
function whole(body) {
    const tail = body.subarray(Math.max(0, body.length - 64)).toString('utf8').trimEnd();
    return tail.endsWith('</html>');
}

function localeCookieOf(headers) {
    const header = [].concat(headers['set-cookie'] ?? []).map(String).find(c => c.startsWith(`${NEXT_LOCALE}=`));
    return header ? {header, value: header.slice(NEXT_LOCALE.length + 1).split(';')[0]} : undefined;
}

/** writeHead takes headers as an object or as the flat `[name, value, …]` list; both read the same here. */
export function headersOf(headers) {
    if (!Array.isArray(headers)) {
        return headers;
    }
    const plain = {};
    for (let i = 0; i + 1 < headers.length; i += 2) {
        const key = String(headers[i]).toLowerCase();
        plain[key] = plain[key] === undefined ? headers[i + 1] : [].concat(plain[key], headers[i + 1]);
    }
    return plain;
}

function lower(headers) {
    return Object.fromEntries(Object.entries(headers ?? {}).map(([name, value]) => [name.toLowerCase(), value]));
}

function replayable(headers) {
    return Object.fromEntries(Object.entries(headers).filter(([name]) => !HOP_HEADERS.has(name)));
}
