/**
 * The page cache's policy: the default table (`config.mjs`) with an environment's changes merged over it, validated
 * once at start, and the classification of a request against it.
 *
 * Precedence, lowest first: the defaults; `STOREFRONT_CACHE_POLICY_JSON` (a partial policy, deep-merged: a class
 * names only the fields it changes); the `overrides` a caller passes (a place for a per-store layer later); the flat
 * variables, which win: `STOREFRONT_CACHE_ENABLED`, `_STORE`, `_MAX_MB`, `_MAX_ENTRY_KB`, `_DEBUG`, `_STATS_TOKEN`,
 * `_EVICTION_LOG_THRESHOLD`, and per class `STOREFRONT_CACHE_<CLASS>_TTL_SECONDS`, `_SWR_SECONDS`, `_ENABLED`
 * (`api-theme-manifest` is `API_THEME_MANIFEST`).
 */
import {DEFAULT_POLICY, ENV_PREFIX, NEXT_RSC_HEADERS, OVERRIDE_COOKIES, OVERRIDE_PARAMS, POLICY_JSON_VARIABLE}
    from './config.mjs';

export class PolicyError extends Error {
    constructor(message) {
        super(`[storefront-cache] ${message}`);
        this.name = 'PolicyError';
    }
}

const LOCALE = /^\/([a-z]{2}(?:-[A-Za-z]{2})?)(?=\/|$)/;

const FLAT_GLOBALS = {
    ENABLED: ['enabled', 'boolean'],
    STORE: ['store', 'string'],
    MAX_MB: ['maxMb', 'number'],
    MAX_ENTRY_KB: ['maxEntryKb', 'number'],
    DEBUG: ['debug', 'boolean'],
    STATS_TOKEN: ['statsToken', 'string'],
    EVICTION_LOG_THRESHOLD: ['evictionLogThreshold', 'number'],
};

const FLAT_CLASS = {
    TTL_SECONDS: ['ttl', 'number'],
    SWR_SECONDS: ['swr', 'number'],
    ENABLED: ['enabled', 'boolean'],
};

/** Builds the policy `start.mjs` runs with. Throws {@link PolicyError} on a value the cache cannot run with. */
export function loadPolicy(env = process.env, {defaults = DEFAULT_POLICY, overrides = []} = {}) {
    let merged = clone(defaults);
    const json = env[POLICY_JSON_VARIABLE];
    if (json !== undefined && json.trim() !== '') {
        merged = merge(merged, parseJson(json), defaults);
    }
    for (const override of overrides) {
        merged = merge(merged, override, defaults);
    }
    merged = merge(merged, flatEnv(env, defaults), defaults);
    return compile(merged);
}

/** One line for the start log: every class with its effective values. */
export function describePolicy(policy) {
    const classes = {};
    for (const [cls, rule] of policy.classes) {
        classes[cls] = rule.enabled
            ? {ttl: rule.ttlMs / 1000, swr: rule.swrMs / 1000, share: rule.share, edge: rule.edge}
            : {enabled: false, edge: rule.edge};
    }
    return JSON.stringify({
        enabled: policy.enabled,
        store: policy.store,
        maxMb: policy.maxBytes / (1024 * 1024),
        maxEntryKb: policy.maxEntryBytes / 1024,
        debug: policy.debug,
        classes,
    });
}

/**
 * Where a request falls in the policy and whether it may be served from the cache at all. `bypass` names the reason
 * when not: `cache-disabled`, `method`, `authorization`, `rsc`, `override-cookie`, `override-param`, `path`,
 * `class-disabled`. The class and its rule are answered either way, since the edge header follows the class.
 */
export function classify(req, policy) {
    const method = req.method ?? 'GET';
    const raw = String(req.url ?? '/');
    const at = raw.indexOf('?');
    const rawPath = at < 0 ? raw : raw.slice(0, at);
    const search = at < 0 ? '' : raw.slice(at + 1);
    const found = matchClass(rawPath, policy);
    const classified = {...found, search};
    const reason = bypassReason(req, method, rawPath, search, policy, classified.rule);
    return reason ? {...classified, bypass: reason} : classified;
}

function matchClass(rawPath, policy) {
    for (const [cls, rule] of policy.classes) {
        if (rule.raw && rule.match(rawPath)) {
            return {cls, rule, path: rawPath, locale: undefined};
        }
    }
    const locale = LOCALE.exec(rawPath)?.[1];
    if (!locale) {
        return {cls: 'next-internal', rule: policy.classes.get('next-internal'), path: rawPath, locale: undefined};
    }
    const path = rawPath.slice(locale.length + 1) || '/';
    for (const [cls, rule] of policy.classes) {
        if (!rule.raw && rule.match(path)) {
            return {cls, rule, path, locale};
        }
    }
    return {cls: 'default', rule: policy.classes.get('default'), path, locale};
}

function bypassReason(req, method, rawPath, search, policy, rule) {
    if (!policy.enabled) {
        return 'cache-disabled';
    }
    if (method !== 'GET' && method !== 'HEAD') {
        return 'method';
    }
    if (req.headers.authorization) {
        return 'authorization';
    }
    if (NEXT_RSC_HEADERS.some(name => req.headers[name] !== undefined)) {
        return 'rsc';
    }
    if (rawPath.startsWith('//')) {
        return 'path';
    }
    if (OVERRIDE_COOKIES.some(cookie => cookieOf(req, cookie) !== undefined)) {
        return 'override-cookie';
    }
    if (search) {
        const params = new URLSearchParams(search);
        if (OVERRIDE_PARAMS.some(param => params.has(param))) {
            return 'override-param';
        }
    }
    if (!rule.enabled) {
        return 'class-disabled';
    }
    return undefined;
}

/** The value of one cookie of the request, or undefined. */
export function cookieOf(req, name) {
    for (const part of String(req.headers.cookie ?? '').split(';')) {
        const at = part.indexOf('=');
        if (at > 0 && part.slice(0, at).trim() === name) {
            return part.slice(at + 1).trim();
        }
    }
    return undefined;
}

function parseJson(json) {
    let parsed;
    try {
        parsed = JSON.parse(json);
    } catch (error) {
        throw new PolicyError(`${POLICY_JSON_VARIABLE} is not JSON: ${error.message}`);
    }
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
        throw new PolicyError(`${POLICY_JSON_VARIABLE} must be an object`);
    }
    return parsed;
}

function flatEnv(env, defaults) {
    const partial = {classes: {}};
    for (const [suffix, [field, type]] of Object.entries(FLAT_GLOBALS)) {
        const value = env[`${ENV_PREFIX}${suffix}`];
        if (value !== undefined && value !== '') {
            partial[field] = coerce(`${ENV_PREFIX}${suffix}`, value, type);
        }
    }
    for (const cls of Object.keys(defaults.classes)) {
        const name = cls.toUpperCase().replace(/-/g, '_');
        for (const [suffix, [field, type]] of Object.entries(FLAT_CLASS)) {
            const variable = `${ENV_PREFIX}${name}_${suffix}`;
            const value = env[variable];
            if (value !== undefined && value !== '') {
                partial.classes[cls] = {...partial.classes[cls], [field]: coerce(variable, value, type)};
            }
        }
    }
    return partial;
}

function coerce(variable, value, type) {
    if (type === 'boolean') {
        if (value === 'true' || value === 'false') {
            return value === 'true';
        }
        throw new PolicyError(`${variable} must be true or false, not ${JSON.stringify(value)}`);
    }
    if (type === 'number') {
        const number = Number(value);
        if (!Number.isFinite(number)) {
            throw new PolicyError(`${variable} must be a number, not ${JSON.stringify(value)}`);
        }
        return number;
    }
    return String(value);
}

function merge(base, partial, defaults) {
    const result = {...base};
    for (const [field, value] of Object.entries(partial)) {
        if (field === 'classes') {
            result.classes = {...base.classes};
            for (const [cls, changes] of Object.entries(value ?? {})) {
                if (!(cls in defaults.classes)) {
                    throw new PolicyError(`unknown class ${JSON.stringify(cls)}; the classes are ${Object.keys(defaults.classes).join(', ')}`);
                }
                if (!changes || typeof changes !== 'object') {
                    throw new PolicyError(`class ${cls} must be an object`);
                }
                result.classes[cls] = {...base.classes[cls], ...changes};
            }
        } else if (!(field in defaults)) {
            throw new PolicyError(`unknown setting ${JSON.stringify(field)}`);
        } else {
            result[field] = value;
        }
    }
    return result;
}

function compile(merged) {
    const seconds = (cls, field, value) => {
        if (!Number.isInteger(value) || value < 0) {
            throw new PolicyError(`class ${cls}: ${field} must be a whole number of seconds, not ${JSON.stringify(value)}`);
        }
        return value * 1000;
    };
    if (typeof merged.enabled !== 'boolean' || typeof merged.debug !== 'boolean') {
        throw new PolicyError('enabled and debug must be true or false');
    }
    if (typeof merged.store !== 'string' || merged.store === '') {
        throw new PolicyError('store must name a store');
    }
    for (const field of ['maxMb', 'maxEntryKb']) {
        if (!(merged[field] > 0)) {
            throw new PolicyError(`${field} must be above zero`);
        }
    }
    if (!Array.isArray(merged.dropParams) || merged.dropParams.some(param => typeof param !== 'string')) {
        throw new PolicyError('dropParams must be a list of parameter names');
    }
    const classes = new Map();
    let shares = 0;
    for (const [cls, rule] of Object.entries(merged.classes)) {
        if (typeof rule.enabled !== 'boolean') {
            throw new PolicyError(`class ${cls}: enabled must be true or false`);
        }
        if (!Array.isArray(rule.match)) {
            throw new PolicyError(`class ${cls}: match must be a list of paths`);
        }
        const edge = rule.edge ?? (rule.enabled ? 'public' : 'private');
        const compiled = {
            cls,
            raw: rule.raw === true,
            match: matcher(rule.match),
            enabled: rule.enabled,
            ttlMs: rule.enabled ? seconds(cls, 'ttl', rule.ttl) : 0,
            swrMs: rule.enabled ? seconds(cls, 'swr', rule.swr) : 0,
            vary: rule.enabled ? [...(rule.vary ?? [])] : [],
            share: rule.enabled ? Number(rule.share ?? 1) : 0,
            edge: edgeOf(cls, edge, rule),
        };
        if (compiled.enabled && compiled.ttlMs === 0) {
            compiled.enabled = false;
        }
        if (compiled.enabled && (compiled.share <= 0 || compiled.share > 1)) {
            throw new PolicyError(`class ${cls}: share must be between 0 and 1`);
        }
        if (compiled.enabled && compiled.vary.length === 0) {
            throw new PolicyError(`class ${cls}: vary must name what keys the page`);
        }
        shares += compiled.enabled ? compiled.share : 0;
        classes.set(cls, compiled);
    }
    if (shares > 1 + 1e-9) {
        throw new PolicyError(`the classes' shares add up to ${shares.toFixed(2)}, above 1`);
    }
    return {
        enabled: merged.enabled,
        store: merged.store,
        maxBytes: Math.floor(merged.maxMb * 1024 * 1024),
        maxEntryBytes: Math.floor(merged.maxEntryKb * 1024),
        debug: merged.debug,
        statsToken: String(merged.statsToken ?? ''),
        evictionLogThreshold: merged.evictionLogThreshold,
        dropParams: [...merged.dropParams],
        classes,
    };
}

function edgeOf(cls, edge, rule) {
    if (edge === 'passthrough') {
        return null;
    }
    if (edge === 'private') {
        return 'private, no-store';
    }
    if (edge === 'public') {
        if (!rule.enabled) {
            return 'private, no-store';
        }
        return `public, s-maxage=${rule.ttl}, stale-while-revalidate=${rule.swr}`;
    }
    if (typeof edge === 'string' && edge.trim() !== '') {
        return edge.trim();
    }
    throw new PolicyError(`class ${cls}: edge must be public, private, passthrough or a Cache-Control value`);
}

/** `/x` matches `/x` alone; `/x/*` matches `/x` and everything below it; `/` matches the root alone. */
function matcher(patterns) {
    const exact = new Set();
    const prefixes = [];
    for (const pattern of patterns) {
        if (typeof pattern !== 'string' || !pattern.startsWith('/')) {
            throw new PolicyError(`a match pattern must start with /, not ${JSON.stringify(pattern)}`);
        }
        if (pattern.endsWith('/*')) {
            prefixes.push(pattern.slice(0, -2));
        } else {
            exact.add(pattern);
        }
    }
    return path => exact.has(path) || prefixes.some(prefix => path === prefix || path.startsWith(`${prefix}/`));
}

function clone(value) {
    return JSON.parse(JSON.stringify(value));
}
