import assert from 'node:assert/strict';
import {spawnSync} from 'node:child_process';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import test from 'node:test';
import {fileURLToPath} from 'node:url';

const script = fileURLToPath(new URL('./copy-instrumentation.mjs', import.meta.url));
const ALIAS = 'require-in-the-middle-0123456789abcdef';
const SCOPED_ALIAS = '@scope/pkg-0123456789abcdef';

/**
 * A build as Next 16.3 leaves it: the hook, its trace listing Turbopack's hashed aliases, the alias links in
 * `.next/node_modules`, and a standalone tree rooted at the workspace (holding the packages the links point at, unless
 * `withPackages` is false).
 */
function build({aliases = true, withPackages = true} = {}) {
    const app = path.join(fs.mkdtempSync(path.join(os.tmpdir(), 'copy-instrumentation-')), 'storefront');
    const server = path.join(app, '.next', 'server');
    fs.mkdirSync(server, {recursive: true});
    fs.writeFileSync(path.join(app, 'package.json'), JSON.stringify({dependencies: {}}));
    fs.writeFileSync(path.join(server, 'instrumentation.js'), 'module.exports = {};\n');
    const files = ['../../../node_modules/require-in-the-middle/index.js'];
    if (aliases) {
        files.push(`../node_modules/${ALIAS}`, `../node_modules/${SCOPED_ALIAS}`);
        fs.mkdirSync(path.join(app, '.next', 'node_modules', '@scope'), {recursive: true});
        fs.symlinkSync('../../../node_modules/require-in-the-middle', path.join(app, '.next', 'node_modules', ALIAS));
        fs.symlinkSync('../../../../node_modules/@scope/pkg', path.join(app, '.next', 'node_modules', SCOPED_ALIAS));
    }
    fs.writeFileSync(path.join(server, 'instrumentation.js.nft.json'), JSON.stringify({version: 1, files}));
    const standalone = path.join(app, '.next', 'standalone');
    fs.mkdirSync(path.join(standalone, 'storefront', '.next', 'server'), {recursive: true});
    if (withPackages) {
        fs.mkdirSync(path.join(standalone, 'node_modules', 'require-in-the-middle'), {recursive: true});
        fs.mkdirSync(path.join(standalone, 'node_modules', '@scope', 'pkg'), {recursive: true});
    }
    return {app, served: path.join(standalone, 'storefront', '.next')};
}

const run = (cwd) => spawnSync(process.execPath, [script], {cwd, encoding: 'utf8', env: {...process.env, NEXT_DIST_DIR: ''}});

test('links every hashed alias the hook requires into standalone, as the same relative link', () => {
    const {app, served} = build();
    const result = run(app);
    assert.equal(result.status, 0, result.stderr);
    assert.ok(fs.existsSync(path.join(served, 'server', 'instrumentation.js')));
    assert.equal(fs.readlinkSync(path.join(served, 'node_modules', ALIAS)), '../../../node_modules/require-in-the-middle');
    assert.equal(fs.readlinkSync(path.join(served, 'node_modules', SCOPED_ALIAS)), '../../../../node_modules/@scope/pkg');
    assert.ok(fs.statSync(path.join(served, 'node_modules', ALIAS)).isDirectory(), 'the link resolves inside standalone');
    assert.match(result.stdout, /2 external alias\(es\) linked/);
});

test('fails the build when an alias points at a package standalone does not have', () => {
    const {app} = build({withPackages: false});
    const result = run(app);
    assert.equal(result.status, 1);
    assert.match(result.stderr, /standalone does not have/);
});

test('copies the build\'s serverExternalPackages into standalone with their dependency closure', () => {
    const {app} = build({aliases: false});
    const pkg = (name, dependencies = {}) => {
        const dir = path.join(app, 'node_modules', ...name.split('/'));
        fs.mkdirSync(dir, {recursive: true});
        fs.writeFileSync(path.join(dir, 'package.json'), JSON.stringify({name, version: '1.0.0', dependencies}));
        fs.writeFileSync(path.join(dir, 'index.js'), 'module.exports = {};\n');
    };
    pkg('@scope/sdk', {'sdk-dep': '1.0.0'});
    pkg('sdk-dep');
    pkg('unrelated');
    fs.writeFileSync(path.join(app, '.next', 'required-server-files.json'),
        JSON.stringify({config: {serverExternalPackages: ['@scope/sdk']}}));
    const result = run(app);
    assert.equal(result.status, 0, result.stderr);
    const modules = path.join(app, '.next', 'standalone', 'node_modules');
    assert.ok(fs.existsSync(path.join(modules, '@scope', 'sdk', 'index.js')));
    assert.ok(fs.existsSync(path.join(modules, 'sdk-dep', 'package.json')), 'its dependencies come along');
    assert.equal(fs.existsSync(path.join(modules, 'unrelated')), false, 'and nothing else');
    assert.match(result.stdout, /external: @scope\/sdk/);
});

test('a build without aliases (Next before 16.3) links nothing', () => {
    const {app, served} = build({aliases: false});
    const result = run(app);
    assert.equal(result.status, 0, result.stderr);
    assert.equal(fs.existsSync(path.join(served, 'node_modules')), false);
    assert.match(result.stdout, /0 external alias\(es\) linked/);
});
