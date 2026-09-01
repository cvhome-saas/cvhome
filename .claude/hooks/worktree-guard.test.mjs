#!/usr/bin/env node
/**
 * Self-test for worktree-guard.mjs. Run it from anywhere in the repo:
 *
 *   node .claude/hooks/worktree-guard.test.mjs
 *
 * The guard is the only thing standing between an agent and a dirty `main`, and its allow-list is
 * the part most likely to be edited later — one careless entry and `.claude/` stops being the
 * exception it is meant to be. Exit 2 means deny, 0 means allow; every case below asserts which.
 *
 * Runs from both the primary checkout and a real worktree, because the two disagree about what
 * `process.cwd()` means and the guard resolves the primary checkout via `--git-common-dir`
 * precisely so they cannot.
 */
import {execFileSync} from 'node:child_process';
import {mkdtempSync, rmSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {dirname, join, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const GUARD = resolve(dirname(fileURLToPath(import.meta.url)), 'worktree-guard.mjs');

/*
 * The PRIMARY checkout, resolved the same way the guard resolves it. Deliberately not
 * `--show-toplevel`: that returns whichever worktree the test happens to be running from, so the
 * "denied on main" cases would silently assert paths inside a worktree and pass for the wrong
 * reason. This test is usually run from a worktree — that is the whole point of the repo — so the
 * distinction is not hypothetical.
 */
const PRIMARY = resolve(
  execFileSync('git', ['rev-parse', '--path-format=absolute', '--git-common-dir'], {
    cwd: dirname(GUARD),
    encoding: 'utf8',
  }).trim(),
  '..',
);

const DENY = 2;
const ALLOW = 0;

/** Runs the guard with `payload` on stdin and returns its exit code. */
function guard(payload, {cwd = PRIMARY, env = {}} = {}) {
  try {
    execFileSync('node', [GUARD], {
      cwd,
      input: typeof payload === 'string' ? payload : JSON.stringify(payload),
      env: {...process.env, ...env},
      stdio: ['pipe', 'ignore', 'ignore'],
    });
    return 0;
  } catch (error) {
    return error.status ?? -1;
  }
}

const file = (path) => ({tool_input: {file_path: path}});

let failed = 0;
function check(expected, name, payload, options) {
  const actual = guard(payload, options);
  const ok = actual === expected;
  if (!ok) failed++;
  console.log(`${ok ? 'ok  ' : 'FAIL'}  ${name} (expected ${expected}, got ${actual})`);
}

/*
 * A scratch worktree, so the "allowed because it is a worktree" cases are testing the real
 * mechanism rather than a path that merely looks like one.
 */
const scratch = mkdtempSync(join(tmpdir(), 'guard-test-'));
const worktree = join(scratch, 'wt');
execFileSync('git', ['worktree', 'add', '--detach', worktree, 'HEAD'], {
  cwd: PRIMARY,
  stdio: 'ignore',
});

try {
  console.log('# from the primary checkout');
  check(DENY, 'a feature file on main', file('store-core/console-ui/src/styles.css'));
  check(DENY, 'AGENTS.md itself', file('AGENTS.md'));
  check(DENY, 'a new file at the repo root', file('scratch.txt'));
  check(ALLOW, 'a plan (.AGENTS/plans)', file('.agents/plans/anything.md'));
  check(ALLOW, 'a plan (plan mode)', file('.claude/plans/anything.md'));
  check(ALLOW, 'the guard config', file('.claude/settings.json'));
  check(ALLOW, 'the personal override', file('.claude/settings.local.json'));
  check(ALLOW, 'the guard itself', file('.claude/hooks/worktree-guard.mjs'));
  check(ALLOW, 'a worktree path', file('.claude/worktrees/feat-x/store-core/x.css'));
  check(ALLOW, 'outside the repo', file('/tmp/scratch/x.txt'));
  check(ALLOW, 'the home directory', file(join(process.env['HOME'] ?? '/root', '.claude/x.json')));

  console.log('\n# malformed input never blocks');
  check(ALLOW, 'no file_path in the payload', {tool_input: {}});
  check(ALLOW, 'no tool_input at all', {});
  check(ALLOW, 'unparseable stdin', 'not json');
  check(ALLOW, 'an empty path', file(''));

  console.log('\n# from inside a worktree');
  check(ALLOW, 'a relative path in this worktree', file('store-core/console-ui/src/styles.css'), {cwd: worktree});
  check(ALLOW, 'an absolute path in this worktree', file(join(worktree, 'AGENTS.md')), {cwd: worktree});
  check(DENY, 'reaching back to the primary checkout', file(join(PRIMARY, 'AGENTS.md')), {cwd: worktree});

  console.log('\n# traversal and the escape hatch');
  check(DENY, '../ out of a worktree lands on main', file('.claude/worktrees/feat-x/../../../AGENTS.md'));
  check(ALLOW, 'CVHOME_ALLOW_MAIN_WRITES=1', file('AGENTS.md'), {env: {CVHOME_ALLOW_MAIN_WRITES: '1'}});
} finally {
  execFileSync('git', ['worktree', 'remove', '--force', worktree], {cwd: PRIMARY, stdio: 'ignore'});
  rmSync(scratch, {recursive: true, force: true});
}

console.log(failed === 0 ? '\nAll cases passed.' : `\n${failed} case(s) failed.`);
process.exit(failed === 0 ? 0 : 1);
