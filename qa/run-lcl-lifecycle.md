# QA — local stack lifecycle script

`extra/scripts/run-lcl.sh` owns local stack startup, shutdown, targeted service recovery, logs, and pid/status
inspection. These cases prove the script can manage the full stack and selected services without accidentally
tearing down unrelated work.

- **Scope** — `extra/scripts/run-lcl.sh`, local Docker infra, Java services, frontends
- **Change** — local lifecycle hardening for `start`, `stop`, `restart`, `-d`, `--list`, `pid`, and `logs`
- **Cases** — 13

Each case is tagged:

- **[verified]** — run against this branch and passed.
- **[not verified]** — documented but not yet run end to end.

---

## 00 — Before you start

Run from the repository root. Docker must be running, and the local hosts file should already be configured.

```bash
sudo ./extra/scripts/configure-domain.sh
bash -n extra/scripts/run-lcl.sh
bash extra/scripts/run-lcl.sh --list
```

Use these probes while testing:

```bash
bash extra/scripts/run-lcl.sh pid
bash extra/scripts/run-lcl.sh --list
lsof -nP -iTCP:8125 -sTCP:LISTEN
lsof -nP -iTCP:8010 -sTCP:LISTEN
docker compose -f docker-compose-lcl.yml ps
```

Stop the stack through the script, not with manual `kill`:

```bash
bash extra/scripts/run-lcl.sh stop
```

---

## FUL — Full stack lifecycle

### FUL-01 — Foreground start opens the whole stack · critical · [verified]

- **Setup** — no recorded stack is running.
- **Steps** — run `bash extra/scripts/run-lcl.sh start` in terminal 1.
- **Expect** — infra starts, Java services start in dependency order, frontends start, and `pid` shows a
  supervisor plus service pids. Expected key ports include `8000`, `8001`, `8020`, `8021`, `8022`, `8010`,
  `8011`, `8110`, `8120`, `8121`, `8122`, `8123`, `8124`, and `8125`.
- **Seen** — foreground supervisor `73983`; `payment` `74574`, `gateway` `74328`, `catalog` `74473`,
  `seller-ui` `74640`; all expected services reached their ports.

### FUL-02 — Full stop cleans services, logs, pids, and volumes · critical · [verified]

- **Setup** — FUL-01 is running.
- **Steps** — from terminal 2, run `bash extra/scripts/run-lcl.sh stop`.
- **Expect** — terminal 1 exits cleanly. `--list` reports stopped services, `build/lcl-runtime/services` is
  gone, `build/lcl-logs` is gone, key service ports are closed, and compose containers/volumes are removed.
- **Seen** — `--list payment gateway catalog seller-ui` reported stopped, `docker compose ... ps` was empty,
  ports `8000`, `8010`, `8122`, and `8125` were closed, and runtime/log paths were removed.

### FUL-03 — Detached start returns only after requested ports are ready · high · [not verified]

- **Setup** — no recorded stack is running.
- **Steps** — run `bash extra/scripts/run-lcl.sh start -d`, then immediately run `pid` and `--list`.
- **Expect** — the command returns after ports are open. `build/lcl-stack.log` exists, the supervisor keeps
  running in the background, and `stop` still performs full cleanup.
- **Seen** — in the Codex command runner, `start -d` returned `ready. supervisor 72102`, but the runner later
  reaped the background supervisor. Re-run manually in a normal terminal before marking verified.

### FUL-04 — Full detached restart replaces the full stack · high · [not verified]

- **Setup** — full stack is running.
- **Steps** — record the supervisor pid, run `bash extra/scripts/run-lcl.sh restart -d`, then run `pid`.
- **Expect** — the old supervisor is gone, a new supervisor is recorded, services reopen their ports, and logs
  are fresh for the restarted stack.

---

## SEL — Selected service lifecycle

### SEL-01 — Stop one Java service without killing the stack · critical · [verified]

- **Setup** — full stack is running. Record supervisor, `payment`, `gateway`, and `catalog` pids.
- **Steps** — run `bash extra/scripts/run-lcl.sh stop payment`.
- **Expect** — only `payment` stops. Port `8125` closes, `pid payment` reports stopped, the supervisor pid is
  unchanged, and unrelated ports such as `8000` and `8122` remain open.
- **Seen** — `stop payment` closed `8125`; supervisor stayed `73983`; `gateway` stayed `74328`, `catalog`
  stayed `74473`, and `seller-ui` stayed `74640`.

### SEL-02 — Start one stopped Java service with `-d` from another terminal · critical · [verified]

- **Setup** — SEL-01 has left `payment` stopped while the foreground supervisor still runs.
- **Steps** — run `bash extra/scripts/run-lcl.sh start -d payment`.
- **Expect** — the command returns after `payment` is ready, port `8125` opens, `payment` has a new pid, and the
  main foreground terminal does not shut down the rest of the stack.
- **Seen** — `start -d payment` returned `ready: payment`; `payment` reopened as pid `75191`, then later
  `75817` in the `port-used` case; supervisor and unrelated service pids stayed stable.

### SEL-03 — Restart one Java service only · critical · [verified]

- **Setup** — full stack is running and `payment` is healthy.
- **Steps** — record `payment`, `gateway`, and `catalog` pids; run
  `bash extra/scripts/run-lcl.sh restart payment`.
- **Expect** — `payment` pid changes and port `8125` reopens. `gateway` and `catalog` pids remain stable.
- **Seen** — after routing selected restart through the supervisor, `restart payment` replaced `75191` with
  `75306`; `gateway` stayed `74328`, `catalog` stayed `74473`, and `seller-ui` stayed `74640`.

### SEL-04 — Stop and restart one frontend only · high · [verified]

- **Setup** — full stack is running and `seller-ui` is healthy.
- **Steps** — run `bash extra/scripts/run-lcl.sh stop seller-ui`, then
  `bash extra/scripts/run-lcl.sh start -d seller-ui`.
- **Expect** — port `8010` closes and reopens, `seller-ui` receives a new pid, and Java services remain
  running.
- **Seen** — `seller-ui` stopped from `74640`, `8010` closed, then `start -d seller-ui` reopened it as pid
  `75640`; `gateway` and `payment` stayed running.

---

## STA — Status, logs, and rejected commands

### STA-01 — `--list` reports running, stopped, and port-used states · high · [verified]

- **Setup** — full stack is running.
- **Steps** — check `--list`; stop `payment` and check again. For `port-used`, stop the stack, bind a test
  process to `8125`, then run `bash extra/scripts/run-lcl.sh --list payment`.
- **Expect** — running services show `running <pid>`, stopped services show `stopped`, and an externally held
  port shows `port-used <pid>`.
- **Seen** — running rows showed pids, stopped `payment` showed `stopped`, and a temporary `nc -l 8125`
  listener showed `payment        port-used 75782`.

### STA-02 — `pid` reports supervisor and selected service pids · high · [verified]

- **Setup** — full stack is running.
- **Steps** — run `bash extra/scripts/run-lcl.sh pid` and `bash extra/scripts/run-lcl.sh pid payment`.
- **Expect** — the first command includes supervisor and service pid rows; the selected command reports only
  `payment`.
- **Seen** — `pid payment` reported `payment        75306 running` while payment was up.

### STA-03 — `logs` tails existing selected logs · [verified]

- **Setup** — full stack is running and `payment` has started at least once.
- **Steps** — run `bash extra/scripts/run-lcl.sh logs payment`, then stop the tail with `Ctrl-C`.
- **Expect** — the command tails `build/lcl-logs/payment.log` and does not affect the running service.
- **Seen** — a bounded tail of `logs payment` produced output and exited after the test killed only the tail
  process.

### STA-04 — Invalid selected-service volume operations are refused · high · [verified]

- **Steps** — run `bash extra/scripts/run-lcl.sh stop --volumes payment` and
  `bash extra/scripts/run-lcl.sh restart --volumes payment`.
- **Expect** — both commands exit non-zero with a clear message that `--volumes` cannot be used with selected
  services.
- **Seen** — both commands exited `1` with the expected refusal messages.

### STA-05 — Unknown service names are refused · high · [verified]

- **Steps** — run `bash extra/scripts/run-lcl.sh start -d missing-service`,
  `bash extra/scripts/run-lcl.sh stop missing-service`, and
  `bash extra/scripts/run-lcl.sh restart missing-service`.
- **Expect** — each exits non-zero with `unknown service: missing-service`.
- **Seen** — all three unknown-service commands exited `1` with `unknown service: missing-service`.

---

## REG — Regression watchlist

| Regression | How it looks | Covered by |
|---|---|---|
| `start -d payment` kills the foreground full stack | terminal 1 shuts down all services after terminal 2 starts payment | SEL-02 |
| Targeted restart leaves the port closed | `payment` pid changes or disappears, but `8125` never reopens | SEL-03 |
| Targeted stop deletes full logs or runtime state | unrelated service pids disappear after `stop payment` | SEL-01 |
| `--list` hides useful status | output lacks `running <pid>`, `stopped`, or `port-used <pid>` | STA-01 |
| Selected restart starts from the short-lived command shell | service briefly opens its port, then disappears after the command exits | SEL-03 |

## 99 — Known gaps

- Full stack startup can fail for service-level reasons unrelated to this script, such as a database migration
  or app boot failure. In that case, attach `build/lcl-logs/<service>.log` and `build/lcl-stack.log`.
- `logs` is intentionally a following tail. Stop it with `Ctrl-C`; that should not stop the stack.

Attach the exact command, terminal output, and relevant files from `build/lcl-logs/` when raising a finding.
