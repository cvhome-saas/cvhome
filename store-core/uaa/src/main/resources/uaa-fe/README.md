# uaa-fe

The identity console and the sign-in page, served by uaa itself from `src/main/resources/static`.

Angular 20 on [`@cvhome-saas/ui-kit`](../../../../../../store-commons/ui-kit) — the same components and uaa API
services console-ui uses, so a screen built here can move there. Transloco carries English and Arabic, and the
whole console is checked in both, right-to-left included.

## Running it

The app talks to a real uaa, so start the stack first:

```bash
lcl start -d --stack <your-worktree>     # from the worktree root
npm run kit                              # build the ui-kit this app compiles against
npm start                                # ng serve on :4300, proxying the API to uaa
```

`npm run kit` first, always: the app compiles against the kit's *built* output, so a kit change that is not
rebuilt shows up as a type error about a symbol you can see in the source. If a new kit export still will not
resolve in the dev server, clear the Angular cache (`rm -rf .angular/cache`) — Vite's pre-bundle outlives a
rebuild.

Signing in needs an account from the seed data: `super-admin` / `admin` in a local stack.

## Building it

```bash
npm run build      # ng build; the Gradle build runs this and copies dist into static/
npm run lint       # eslint, stylelint, and the lessons.md citation check
npm test           # Karma, headless Chrome
```

The Gradle build of `store-core:uaa` runs `npm run build` and copies the output, so **a frontend that does not
compile fails the Java build**. That is deliberate: the jar must never ship a console that does not match its
backend.

## Where things are

| Path | What |
|---|---|
| `src/app/features/*` | one folder per screen: users, roles, clients, identity providers, settings, audit, dashboard, account, sign-in, link-accept |
| `src/app/features/*/facades` | the state and the calls for a screen; components stay declarative |
| `src/app/layouts/admin-shell` | the rail, the breadcrumb, the locale switcher, the account menu |
| `src/locale/{en,ar}.json` | every string, in both languages — a key added to one and not the other is a bug |
| `lessons.md` | what the console deliberately does not draw, and why |

## lessons.md

Append-only, and cited from the code. When a screen backs away from the design — a control the backend cannot
support, a number nobody can compute honestly — it goes there with the reason, and the component that made the
call cites the heading. `npm run lint` fails if a citation names a heading that does not exist, so the two
cannot drift apart. Closing an entry means adding a "Closed by …" line, never deleting the entry.
