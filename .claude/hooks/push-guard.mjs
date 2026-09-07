#!/usr/bin/env node
/**
 * Refuses `git push` from an agent unless the tree has passed the local pipeline run.
 *
 * AGENTS.md, "Working conventions": *a push is gated on `extra/scripts/verify-before-push.sh`*. The
 * git `pre-push` hook enforces that for everyone whose clone has the hook installed; this is the same
 * gate one layer earlier, as a `PreToolUse` check on the Bash tool, for two reasons. An agent's clone
 * may not have `core.hooksPath` set yet (the script sets it on first run — which is the run that
 * has not happened). And an agent that hits the git hook can reach for `--no-verify`; this hook
 * refuses that spelling outright, with the reason on stderr, which is the one channel that reliably
 * reaches the model.
 *
 * The receipt is `<git-dir>/cvhome-verified`: a sha256 over HEAD, `git status --porcelain`, and
 * `git diff HEAD`, written by the script when every check passed. The same digest is recomputed here;
 * a mismatch means something changed since the green run — commit or edit — and the answer is to run
 * the script again, not to push and let the pipeline find out.
 *
 * Escape hatch: `SKIP_VERIFY=1` in the environment, the same one the git hook honours. An env var
 * rather than a flag so the decision stays with the person running the session.
 */
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
import {existsSync, readFileSync} from 'node:fs';
import {join} from 'node:path';

/** `stdin` is one JSON object: `{tool_name, tool_input: {command, ...}, ...}`. */
let input;
try {
  input = JSON.parse(readFileSync(0, 'utf8'));
} catch {
  process.exit(0);
}

const command = input?.tool_input?.command;
if (typeof command !== 'string' || !/\bgit\b[^|;&]*\bpush\b/.test(command)) {
  process.exit(0);
}

if (process.env['SKIP_VERIFY'] === '1') {
  process.exit(0);
}

if (/--no-verify\b/.test(command)) {
  process.stderr.write(
    `Blocked: git push --no-verify.\n\n` +
      `The pre-push hook is the local pipeline run; skipping it is how a red pipeline happens.\n` +
      `Run extra/scripts/verify-before-push.sh and push without --no-verify.\n`,
  );
  process.exit(2);
}

/*
 * The worktree the push comes from. A `git -C <dir>` or a leading `cd <dir> &&` names it; otherwise
 * it is the session's working directory.
 */
const dirMatch = /\bgit\s+-C\s+(\S+)/.exec(command) ?? /^\s*cd\s+(\S+)\s*&&/.exec(command);
const cwd = dirMatch ? dirMatch[1].replace(/^["']|["']$/g, '') : process.cwd();

function git(args) {
  return execFileSync('git', args, {cwd, encoding: 'utf8', stdio: ['ignore', 'pipe', 'ignore']});
}

let gitDir;
let current;
try {
  gitDir = git(['rev-parse', '--absolute-git-dir']).trim();
  const head = git(['rev-parse', 'HEAD']);
  const status = git(['status', '--porcelain=v1', '--untracked-files=all']);
  const diff = git(['diff', 'HEAD']);
  current = createHash('sha256').update(head + status + diff).digest('hex');
} catch {
  // Not a git repo from here, or no git on PATH: nothing to guard, and git itself will say so.
  process.exit(0);
}

const receipt = join(gitDir, 'cvhome-verified');
if (existsSync(receipt) && readFileSync(receipt, 'utf8').trim() === current) {
  process.exit(0);
}

process.stderr.write(
  `Blocked: this tree has not passed the local pipeline run.\n\n` +
    (existsSync(receipt)
      ? `The last receipt is for a different tree — something was committed or edited since it was written.\n\n`
      : `No receipt exists for this worktree yet.\n\n`) +
    `Run the whole pipeline locally, then push:\n\n` +
    `  extra/scripts/verify-before-push.sh\n\n` +
    `It runs checkstyle, the build, unit and integration tests, the coverage floors, and both frontends'\n` +
    `lint and tests — the same checks the pipeline runs — and writes the receipt the push hooks read.\n` +
    `Never --no-verify. SKIP_VERIFY=1 is the person's escape hatch, not the agent's.\n`,
);
process.exit(2);
