# console-ui

The seller console for cvhome — the application a merchant signs into to run their stores.

Angular 20 (standalone, SSR), Tailwind v4 over a three-theme token layer, Transloco for English and
Arabic with full right-to-left support. It replaced `store-core/seller-ui` module by module (the old
console's last commit is tagged `seller-ui-final`); `lessons.md` records what the platform cannot do
yet and what the console does instead.

## Running it

The console expects the gateway, tenancy and the pods to be up, so the usual way to run it is as
part of the local stack:

```bash
./run-lcl.sh            # from the repository root — starts everything, console-ui included
restart console-ui      # rebuild just this app; never `kill` it, the stack tracks its pid
```

Then open `http://console-ui.gateway.com` (`configure-domain.sh` adds the hosts entry).

On its own, against whatever the environment points at:

```bash
npm install
npm run dev             # http://localhost:4200
```

## Working on it

```bash
npm run build           # AOT. Run before committing — strict template checking lives here
npm run lint            # eslint + stylelint + lessons citations + unused i18n keys
npm run test:ci         # non-interactive
```

## Documentation

| | |
|---|---|
| [`ARCHITECTURE.md`](./ARCHITECTURE.md) | The tiers, the shape of a feature, the shared control catalogue, and the rules a new page follows. |
| [`DESIGN.md`](./DESIGN.md) | The visual system: tokens, the three themes, the named rules. Read by the design-review tooling. |
| [`lessons.md`](./lessons.md) | Every backend capability the console needs and does not have. Append-only, one entry per gap. |
| [`CLAUDE.md`](./CLAUDE.md) | The short version, plus the rules that are easiest to break. |
