import { AngularAppEngine } from '@angular/ssr';
import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

/**
 * Accept every `Host`. The console is never addressed directly — store-core-gateway is the only thing that
 * reaches it, and that gateway already pins the host: its console route matches `<domain>`, `www.<domain>` and
 * `console-ui.<domain>` and nothing else. So the SSRF allowlist this disables is a second copy of a decision
 * already made one tier up, and the only copy that has to be told each environment's domain at *build* time.
 *
 * It was not merely redundant, it was wrong: Spring Cloud Gateway resolves `lb://console-ui` through Cloud Map,
 * so on Fargate the `Host` it forwards is the task's private IP — `10.0.0.97:8011` — which no allowlist can name
 * ahead of time. Every request was answered `Header "host" with value "10.0.0.97:8011" is not allowed.` with a
 * 400. Locally discovery resolves to `localhost:8011`, which the allowlist did name, so it only broke deployed.
 *
 * `security.allowedHosts: ["*"]` in `angular.json` is the config that is *supposed* to express this. It does not:
 * `@angular/ssr` 20.3.35 warns that `"*"` allows every host and then matches it with a rule that only understands
 * the `*.suffix` form, so a bare `"*"` allows nothing. Verified against the built server. The flag below is what
 * actually does it; keep the `"*"` so this reads correctly from `angular.json`, and drop the flag once a release
 * honours it.
 */
AngularAppEngine.ɵdisableAllowedHostsCheck = true;

const browserDistFolder = join(import.meta.dirname, '../browser');
const SUPPORTED_LOCALES = ['en', 'ar'];

const app = express();
const angularApp = new AngularNodeAppEngine();

/**
 * `Content-Language` header only — the actual render locale is resolved client-side by
 * `LANG_STORAGE`/Transloco from the same cookie. This just needs to agree with that so the
 * header is not misleading. Cookie wins over `Accept-Language`: an explicit choice a seller
 * made in the UI should not be overridden by whatever their OS/browser reports.
 */
function resolveLocale(req: express.Request): string {
  const cookieLocale = req.headers.cookie?.match(/(?:^|;\s*)cvhome\.console\.lang=(en|ar)/)?.[1];
  if (cookieLocale) {
    return cookieLocale;
  }
  const acceptLanguage = req.headers['accept-language'] ?? '';
  return SUPPORTED_LOCALES.find((locale) => acceptLanguage.includes(locale)) ?? 'en';
}

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * ```ts
 * app.get('/api/{*splat}', (req, res) => {
 *   // Handle API request
 * });
 * ```
 */

/**
 * Serve static files from /browser
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
  }),
);

/**
 * Handle all other requests by rendering the Angular application.
 */
app.use((req, res, next) => {
  res.setHeader('Content-Language', resolveLocale(req));
  res.append('Vary', 'Accept-Language, Cookie');

  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 8011.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 8011;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
