/*
 * @cvhome-saas/ui-kit — the primary entry point.
 *
 * Application infrastructure: configuration, the HTTP client, the error stack, auth, platform
 * access, table types, routing helpers and the wire shapes those need. Everything here is free of
 * any one application's vocabulary; anything that knows about stores, orders or a particular
 * console's routes belongs in the consumer.
 *
 * The rest of the design system lives in the secondary entry points: `/ui`, `/theme`, `/i18n`,
 * `/forms`, `/uaa`. A secondary entry point may import this one; the reverse is a cycle and a hard
 * ng-packagr error.
 */

// -- configuration
export * from './lib/config/ui-kit.config';
export * from './lib/config/provide-ui-kit';

// -- http
export * from './lib/http/crud.service';
export * from './lib/http/optional';
export * from './lib/http/request-context';

// -- the error stack
export * from './lib/errors/api-error';
export * from './lib/errors/api-error.interceptor';
export * from './lib/errors/api-error.service';
export * from './lib/errors/form-error.utils';
export * from './lib/errors/global-error-handler';
export * from './lib/errors/notification.port';
export * from './lib/errors/problem-detail.model';
export * from './lib/errors/problem-detail.parser';
export * from './lib/errors/problem-message';
export * from './lib/errors/session.service';

// -- auth
export * from './lib/auth/auth.service';
export * from './lib/auth/auth-guard.service';
export * from './lib/auth/roles';

// -- platform
export * from './lib/platform/browser-storage';
export * from './lib/platform/clipboard';

// -- routing, resources
export * from './lib/routing/confirm-leave.guard';
export * from './lib/routing/route-data';
export * from './lib/routing/route-params';
export * from './lib/state/snapshot';

// -- the wire shapes the above are written in terms of
export * from './lib/models/page';
export * from './lib/models/ui';
export * from './lib/models/locale';
export * from './lib/models/auth';
export * from './lib/models/reference';
