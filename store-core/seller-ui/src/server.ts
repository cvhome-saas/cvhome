import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import {dirname, resolve} from 'node:path';
import {fileURLToPath} from 'node:url';

const serverDistFolder = dirname(fileURLToPath(import.meta.url));
const browserDistFolder = resolve(serverDistFolder, '../browser');
const indexHtml = resolve(browserDistFolder, 'index.csr.html');

const app = express();
const angularApp = new AngularNodeAppEngine();

// Add the routes you want to be server-side rendered here.
const ssrRoutes = ['/', "/signup", '/terms', '/privacy-policy'];

/**
 * Example Express Rest API endpoints can be defined here.
 * Uncomment and define endpoints as necessary.
 *
 * Example:
 * ```ts
 * app.get('/api/**', (req, res) => {
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
    redirect: false,
  }),
);

/**
 * Handle SSR requests.
 */
app.get(ssrRoutes, (req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Handle all other requests by serving the client-side application.
 * This enables Client-Side Rendering (CSR) for the rest of the app.
 */
app.get('*', (req, res) => {
  res.sendFile(indexHtml);
});


/**
 * Start the server if this module is the main entry point.
 * The server listens on the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url)) {
  const port = process.env['PORT'] || 8010;
  app.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);
