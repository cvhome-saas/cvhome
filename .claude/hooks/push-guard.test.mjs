#!/usr/bin/env node
/**
 * Self-test for push-guard.mjs. Run it from anywhere in the repo:
 *
 *   node .claude/hooks/push-guard.test.mjs
 *
 * A throwaway git repository stands in for a worktree, so the cases can write and stale the receipt
 * without touching this one. Exit 2 means deny, 0 means allow; every case asserts which.
 */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {mkdtempSync, rmSync, writeFileSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {dirname, join, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const GUARD = resolve(dirname(fileURLToPath(import.meta.url)), 'push-guard.mjs');
const DENY = 2;
const ALLOW = 0;

const repo = mkdtempSync(join(tmpdir(), 'push-guard-'));
const git = (...args) =>
  execFileSync('git', args, {cwd: repo, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore']});
git('init', '-q');
git('config', 'user.email', 'test@example.com');
git('config', 'user.name', 'test');
writeFileSync(join(repo, 'a.txt'), 'one\n');
git('add', 'a.txt');
git('commit', '-q', '-m', 'one');

function digest() {
  const head = git('rev-parse', 'HEAD');
  const status = git('status', '--porcelain=v1', '--untracked-files=all');
  const diff = git('diff', 'HEAD');
  return createHash('sha256').update(head + status + diff).digest('hex');
}
function writeReceipt(value = digest()) {
  writeFileSync(join(git('rev-parse', '--absolute-git-dir').trim(), 'cvhome-verified'), `${value}\n`);
}

function guard(command, env = {}) {
  try {
    execFileSync('node', [GUARD], {
      cwd: repo,
      input: JSON.stringify({tool_name: 'Bash', tool_input: {command}}),
      env: {...process.env, SKIP_VERIFY: '', ...env},
      stdio: ['pipe', 'ignore', 'ignore'],
    });
    return 0;
  } catch (error) {
    return error.status ?? -1;
  }
}

let failed = 0;
function check(expected, name, command, env) {
  const actual = guard(command, env);
  const ok = actual === expected;
  if (!ok) {
    failed += 1;
  }
  console.log(`${ok ? 'ok  ' : 'FAIL'}  ${name} (expected ${expected}, got ${actual})`);
}

try {
  check(ALLOW, 'a command that is not a push', 'git status && ./gradlew test');
  check(ALLOW, 'a mention of push that is not git', 'echo push it');
  check(DENY, 'a push with no receipt', 'git push -u origin HEAD');
  writeReceipt();
  check(ALLOW, 'a push of the verified tree', 'git push origin HEAD');
  check(ALLOW, 'a push through git -C of the verified tree', `git -C ${repo} push`);
  check(DENY, '--no-verify even with a receipt', 'git push --no-verify');
  writeFileSync(join(repo, 'a.txt'), 'two\n');
  check(DENY, 'an edit after the green run stales the receipt', 'git push');
  check(ALLOW, 'SKIP_VERIFY=1 is the person\'s escape hatch', 'git push', {SKIP_VERIFY: '1'});
  git('add', 'a.txt');
  git('commit', '-q', '-m', 'two');
  check(DENY, 'a commit after the green run stales the receipt', 'git push');
  writeReceipt('not-a-digest');
  check(DENY, 'a receipt that does not match', 'git push');
} finally {
  rmSync(repo, {recursive: true, force: true});
}

if (failed > 0) {
  console.error(`\n${failed} case(s) failed`);
  process.exit(1);
}
console.log('\nall cases passed');
