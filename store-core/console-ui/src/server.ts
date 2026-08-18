import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

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
