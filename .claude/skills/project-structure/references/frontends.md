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
layer, Transloco (en/ar with RTL). Scripts: `ng serve` for dev, `ng build --watch --configuration
development` for watch.

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
module's resources tree. It is built on `@cvhome-saas/ui-kit` — the same component library and uaa API services
console-ui uses — with Transloco for English and Arabic; the Nebular, module-federation and ngx-toastr stack it
started from is gone. It renders both the signed-out sign-in page and the admin console. It is **not** a Gradle module — it never
appears in `settings.gradle` and has no `build.gradle`. It is driven entirely by tasks in
`store-core/uaa/build.gradle`:

```groovy
node {
    version = '23.8.0'
    download = true
    nodeProjectDir = file('src/main/resources/uaa-fe')   // point the node plugin at the nested app
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
Java module each time.

## 3. Server-rendered Thymeleaf

Only `store-core/uaa` renders server-side pages, with `spring-boot-starter-thymeleaf` +
`thymeleaf-extras-springsecurity6`: the staff login pages, alongside its embedded SPA. Templates live under
`src/main/resources/templates/`.

**`store-pod/cua` renders nothing.** It used to serve Thymeleaf login and registration pages, which meant every
store on a pod got the same fixed screen. Now cua is headless: an unauthenticated `/oauth2/authorize` is
answered with a redirect to the storefront's `/{lang}/login?auth=1`, landing-ui renders the login form as a
theme page (`ThemePages.Login`, shell fallback when a theme has none), the form posts straight back to
`/cua/login`, and cua resumes the saved authorize request. Registration is `POST /cua/api/v1/public/registration`
(JSON), rendered by `ThemePages.Register`. Shopper login screens therefore live in `store-pod/landing-ui` —
see `landing-ui.md`.
