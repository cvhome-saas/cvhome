# Frontend patterns

Three different ways UI is delivered in this repo. Know which one you're in before making changes. Creating a
*new* UI service (a `-ui` module, or a Java service with an embedded SPA) is `new-service.md`.

## 1. `-ui` modules — Gradle-wrapped npm apps

`store-core/console-ui` (Angular) and `store-pod/landing-ui` (Next.js). Different frameworks, identical build
contract, because both apply the shared convention plugin
`build-logic/src/main/groovy/com.asrevo.ui-conventions.gradle`:

```groovy
plugins {
    id 'com.asrevo.docker-conventions'
    id 'com.github.node-gradle.node'
}
node { version = '23.8.0'; npmVersion = '11.6.1'; download = true }

tasks.named('build')   { dependsOn('npm_run_build') }        // gradle build → npm run build
tasks.register('bootRun') { dependsOn('npm_run_dev') }       // gradle bootRun → npm run dev
tasks.named('clean')   { delete('dist'); delete('package-lock.json') }
```

Node/npm are **auto-downloaded and version-pinned by Gradle** — you don't need a matching local Node install for
a Gradle build (you do for running `npm` by hand). `docker-conventions` gives both modules container images;
each declares its own `bootBuildImage { imageGroup = ... }` (`store-core` vs `store-pod`).

Each `-ui` module's own `build.gradle` is therefore tiny — just the two plugin aliases and an image group.

### `console-ui` specifics

Angular 20, standalone components, SSR (`app.config.server.ts`, `app.routes.server.ts`,
`serve:ssr:console-ui` → `node dist/console-ui/server/server.mjs`), Tailwind v4 over a three-theme token
layer, Transloco (en/ar with RTL). Scripts: `ng serve`
for dev, `ng build --watch --configuration development` for watch.

Auth is delegated: `src/environments/environment.ts` sets `loginUrl: '/oauth2/authorization/uaa'` and
`apiUrl: ''` — the app is served behind the platform gateway, which owns the session, so it makes same-origin
relative API calls rather than holding tokens.

Feature areas live under `src/app/features/`. Note this module also carries its own `ARCHITECTURE.md`
(the tiering contract) and `lessons.md` (backend gaps the console works around).

**SSR vs. hot reload is already solved in `angular.json`.** The two needs are separated by build configuration:

| configuration | used by | shape |
|---|---|---|
| `production` | `ng build` (its default), the container image | SSR — `server`, `outputMode: server`, `ssr.entry`; emits `dist/console-ui/server/` |
| `development` | `ng serve` / `npm start` / `lcl` | plain CSR, so HMR works |

### `landing-ui` specifics

See `landing-ui.md` — it has its own npm-workspaces structure and template system. The one thing to carry
across: its `npm run build` is a **strictly ordered chain** (libs → templates → app), because templates and
app consume the libs as built output. Building `app` alone compiles against yesterday's types.

## 2. Embedded Angular inside Spring Boot — `uaa-fe`

`store-core/uaa/src/main/resources/uaa-fe` is a complete Angular 20 CLI app that lives **inside** the Java
module's resources tree. It is **not** a Gradle module — it never appears in `settings.gradle` and has no
`build.gradle`. It is driven entirely by tasks in `store-core/uaa/build.gradle`:

Same stack as console-ui and for the same reason: standalone Angular 20, Tailwind v4 on the token layer, and
Transloco en/ar, all consumed from **`@cvhome-saas/ui-kit`** (§4). Nebular, jQuery, Bootstrap, ngx-toastr and
module-federation are gone. Four routes — `sign-in`, `users`, `roles`, `clients` — of which `/login` is the one
Spring Security actually points at.

```groovy
node {
    version = '23.8.0'
    download = true
    nodeProjectDir = file('src/main/resources/uaa-fe')   // point the node plugin at the nested app
}

// npm resolves the kit by reading dist/ui-kit's package.json, so it must exist before install.
tasks.named('npmInstall') {
    dependsOn ':store-commons:ui-kit:build'
}

tasks.register('copyAngularApp', Copy) {
    dependsOn('npm_run_build')                                       // = ng build
    from("${projectDir}/src/main/resources/uaa-fe/dist/uaa-fe/browser")
    into "${project.projectDir}/src/main/resources/static"
}

tasks.named('processResources') {
    dependsOn(tasks.named('copyAngularApp'))
    exclude 'uaa-fe/**'          // keep raw Angular sources out of the jar
}

tasks.named('clean') {
    delete "${project.projectDir}/src/main/resources/static"
}
```

**Flow:** `npm_run_build` (ng build) → output at `uaa-fe/dist/uaa-fe/browser` → `copyAngularApp` copies it into
`src/main/resources/static` → `processResources` (which `bootJar` depends on) runs after that copy and excludes
`uaa-fe/**`. Result: only compiled assets ship in the jar, and Spring Boot's default static-resource handler
serves the SPA from the same origin and port as the UAA service — no separate web server, no CORS.

**Gotcha:** `src/main/resources/static` is generated output, deleted by `clean`. Never hand-edit files there and
never commit them — edit `uaa-fe/` sources instead.

To iterate on `uaa-fe` quickly, run `ng serve` inside `src/main/resources/uaa-fe` rather than rebuilding the
Java module each time. `npm run start` builds the kit first; if you reach straight for `ng serve` after a fresh
clone, run `npm run kit` once or the build fails with `Could not resolve "@cvhome-saas/ui-kit"`.

**Sign-in is a native form POST.** `AppSecurityConfig` declares `formLogin(loginPage("/login"))`, so the page
submits `username`/`password` to `/login` as a real form and Spring Security answers with a redirect. Posting it
through `HttpClient` would strand the OAuth2 authorization flow, because that redirect is what resumes it.

## 3. Server-rendered Thymeleaf

Both authorization servers render classic server-side pages with
`spring-boot-starter-thymeleaf` + `thymeleaf-extras-springsecurity6`:

- **`store-core/uaa`** — staff login pages (alongside its embedded SPA).
- **`store-pod/cua`** — shopper login, registration, and social-login pages. No SPA at all here.

Templates live under each module's `src/main/resources/templates/`. This is where to look for login/consent
screens — they are *not* in any Angular or Next.js app.

## 4. The shared Angular library — `@cvhome-saas/ui-kit`

`store-commons/ui-kit` is a Gradle module (`store-commons:ui-kit`) and an ng-packagr library with six entry
points: the primary one (config, `CrudService`, the error stack, auth, platform access, routing helpers,
`snapshot()`), `/ui` (the control catalogue), `/theme` (the four-theme token layer, shipped as CSS assets),
`/i18n`, `/forms` and `/uaa` (clients for uaa's admin API, which both consoles call).

console-ui and uaa-fe consume the **built** package through a `file:` dependency. Three things make that work,
and each has a failure behind it:

- **`preserveSymlinks: true`** on every consumer's `build` and `test` target. `file:` installs a symlink;
  without this Angular resolves through it and hunts for `@angular/core` from `store-commons/ui-kit`.
- **The kit must be built before a consumer installs.** npm does not fail when `dist/ui-kit` is missing — it
  exits 0 and writes a dangling symlink, and the failure arrives later as
  `Could not resolve "@cvhome-saas/ui-kit"`. Hence the `npm run kit` script, the `npmInstall` `dependsOn`, and
  the `lcl.yml` prepare step.
- **`@source '../node_modules/@cvhome-saas/ui-kit'`** in the consumer's stylesheet. Several controls use real
  Tailwind utilities and ng-packagr does not run Tailwind over component CSS, so only the consumer's build can
  emit them. Omit it and everything compiles with ~2.6 kB of utilities silently missing.

The theme CSS is `@import`ed from inside the consumer's own stylesheet, never listed in `angular.json`'s
`styles`: it is Tailwind v4 `@theme`/`@utility` source and must share the PostCSS pass with
`@import 'tailwindcss'`.

Full detail: `store-commons/ui-kit/README.md`.
