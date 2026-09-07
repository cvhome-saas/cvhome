#!/usr/bin/env bash
#
# Everything the pipeline checks, run here first — and a receipt the push hooks read.
#
# The pipeline failed on coverage more often than on anything else, always after a push that had run
# "the tests" but not the coverage gate, or had run them before the last edit. So this script is the
# pipeline: the same five Gradle invocations the workflows run (quality, build, unit, integration,
# coverage floors), plus the two frontends' lint and unit tests, in that order, stopping at the first
# failure. When everything passes it writes `<git-dir>/cvhome-verified` holding a digest of exactly
# what was verified — HEAD plus every uncommitted change — and `.githooks/pre-push` and the Claude
# `push-guard` hook refuse a push whose tree does not match that digest. Edit one file after a green
# run and the receipt is stale; run this again.
#
#   extra/scripts/verify-before-push.sh          # from anywhere inside the worktree being shipped
#
# Docker must be running (Testcontainers). The whole run takes ten to thirty minutes on a laptop;
# that is the price of a pipeline that is green on the first try. The digest, not a timestamp, is
# what makes the receipt honest: `git diff HEAD` is part of it, so a change that is not yet committed
# is still covered, and committing the verified tree keeps the receipt valid.
set -euo pipefail

root="$(git rev-parse --show-toplevel)"
git_dir="$(git rev-parse --absolute-git-dir)"
cd "$root"

# The receipt's subject: the commit, plus every change on top of it, tracked or not.
digest() {
  {
    git rev-parse HEAD
    git status --porcelain=v1 --untracked-files=all
    git diff HEAD
  } | shasum -a 256 | cut -d' ' -f1
}

# Installs the pre-push hook for this clone the first time it is run; idempotent afterwards.
if [ "$(git config --get core.hooksPath || true)" != ".githooks" ]; then
  git config core.hooksPath .githooks
  echo "verify: git core.hooksPath set to .githooks (pre-push is now enforced for this clone)"
fi

step() {
  echo
  echo "▶ verify: $1"
  shift
  local started
  started=$(date +%s)
  "$@"
  echo "✔ verify: done in $(( $(date +%s) - started ))s"
}

before="$(digest)"

step "quality — checkstyle (warnings are errors)" \
  ./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest -q
step "build — what the build job compiles and packages" \
  ./gradlew build -x test -x check -q
step "unit tests and the test-naming rule" \
  ./gradlew test verifyTestNaming --continue -q
step "integration tests (Docker)" \
  ./gradlew integrationTest --continue -q
step "coverage floors — the gate that fails the pipeline most" \
  ./gradlew compileJava domainCoverage coverageReport printDomainCoverage domainCoverageVerification \
    -x test -x integrationTest -PcoverageFromArtifacts --continue -q
step "ui-kit — lint and unit tests" \
  bash -c 'cd store-commons/ui-kit && npm run lint && npm run test:ci'
step "console-ui — lint (tokens, logical properties, i18n) and unit tests" \
  bash -c 'cd store-core/console-ui && npm run lint && npm run test:ci'

after="$(digest)"
if [ "$before" != "$after" ]; then
  echo
  echo "✘ verify: the tree changed while the checks ran (a build wrote a tracked file, or you edited)."
  echo "  Commit or discard what changed, then run this again — a receipt for a tree that no longer exists is no receipt."
  git status --short | head -20
  exit 1
fi

printf '%s\n' "$after" > "$git_dir/cvhome-verified"
echo
echo "✔ verify: all checks passed — receipt written for $(git rev-parse --short HEAD) ($after)."
echo "  git push is now allowed for exactly this tree."
