/*
 * The harness moved to the kit: it stands a service up against `HttpTestingController` with
 * `UI_KIT_CONFIG` and `REQUEST_CONTEXT` provided, all three of which are the library's, and the
 * kit's own api specs need exactly the same wiring.
 *
 * Re-exported from here so the console's two dozen api-tier specs keep the import they had.
 */
export {apiHarness, verifyNoPendingRequests, TEST_STORE} from '@cvhome-saas/ui-kit';
