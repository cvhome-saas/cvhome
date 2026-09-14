import {strict as assert} from 'node:assert';
import {EventEmitter} from 'node:events';
import {test} from 'node:test';
import {installRequestSignal, REQUEST_SIGNAL, withRequestSignal} from './request-scope.mjs';

/** Just enough of a ServerResponse: it emits 'close', and knows whether it finished. */
function response() {
    const res = new EventEmitter();
    res.writableFinished = false;
    return res;
}

test('the request signal is readable anywhere inside the request, across awaits', async () => {
    installRequestSignal();
    const res = response();
    await withRequestSignal(res, async () => {
        await new Promise(resolve => setImmediate(resolve));
        const signal = globalThis[REQUEST_SIGNAL]();
        assert.ok(signal instanceof AbortSignal);
        assert.equal(signal.aborted, false);
    });
    assert.equal(globalThis[REQUEST_SIGNAL](), undefined, 'outside a request there is no signal');
});

test('a connection closed before the response was sent aborts the signal', async () => {
    installRequestSignal();
    const res = response();
    let seen;
    await withRequestSignal(res, async () => {
        seen = globalThis[REQUEST_SIGNAL]();
    });
    res.emit('close');
    assert.equal(seen.aborted, true);
});

test('a response that finished and then closed aborts nothing', async () => {
    installRequestSignal();
    const res = response();
    let seen;
    await withRequestSignal(res, async () => {
        seen = globalThis[REQUEST_SIGNAL]();
    });
    res.writableFinished = true;
    res.emit('close');
    assert.equal(seen.aborted, false);
});
